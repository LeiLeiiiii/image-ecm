package com.sunyard.sunafm.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.SysParamDTO;
import com.sunyard.sunafm.constant.AfmConstant;
import com.sunyard.sunafm.mapper.AfmServerMapper;
import com.sunyard.sunafm.po.AfmServer;
import com.sunyard.sunafm.util.Base64Utils;
import com.sunyard.sunafm.util.CnnHttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author P-JWei
 * @date 2024/3/20 14:05:53
 * @title
 * @description 通用实体类
 */
@Slf4j
@Service
public class CommonService {
    @Value("${cnn.baseUrl:http://172.1.3.211:9210}")
    private String cnnUrl;
    //最大条数(万)
    @Value("${milvus.collection.maxSize:2140000}")
    private Long maxSize;
    //是否自动切换
    @Value("${milvus.collection.autoSwitch:0}")
    private Integer autoSwitch;
    @Resource
    private AfmServerMapper afmServerMapper;
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private DictionaryApi dictionaryApi;
    @Resource
    private ParamApi paramApi;


    /**
     * 获取默认配置
     */
    public JSONObject getSystemFileNet() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(AfmConstant.FILE_NUM_SYSTEM, AfmConstant.FILE_NUM_MILVUS_DEFULT);
        jsonObject.put(AfmConstant.FILE_SIMILARITY_SYSTEM, AfmConstant.FILE_SIMILARITY_DEFULT);
        Result<SysParamDTO> sysParamDTOResult = paramApi.searchValueByKey(AfmConstant.AFM_PARAM_SYSTEM);
        if (sysParamDTOResult.isSucc()) {
            String value = sysParamDTOResult.getData().getValue();
            jsonObject = JSONObject.parseObject(value);
        }
        return jsonObject;
    }
    /**
     * 获取配置的相似度
     */
    public Double getSimpleDefult() {
        JSONObject systemFileNet = getSystemFileNet();
        return systemFileNet.getDouble(AfmConstant.FILE_SIMILARITY_SYSTEM);
    }


    /**
     * getAfmSource
     */
    public Map<String, String> getAfmSource() {
        try {
            Result<Map<String, String>> descByKey = dictionaryApi.getDescByCode(AfmConstant.AFM_SOURCE);
            return descByKey.getData();
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 来源
     */
    public List<Map> getAfmSourceLabel(String ex) {
        Result<Map<String, String>> descByKey = dictionaryApi.getDescByCode(AfmConstant.AFM_SOURCE);
        Map<String, String> data = descByKey.getData();
        ArrayList<Map> objects = new ArrayList<>();
        for (String key : data.keySet()) {
            if (StringUtils.isNotBlank(ex) && ex.equals(key)) {
                continue;
            }
            Map map1 = new HashMap();
            map1.put("label", data.get(key));
            map1.put("value", key);
            objects.add(map1);
        }

        return objects;
    }

    /**
     * 更新内存大小
     */
    public void queryServer() {
        List<AfmServer> afmServers = afmServerMapper.selectList(null);

        //正在写入的服务器
        List<AfmServer> collect = afmServers.stream().filter(s -> s.getCollectionName() != null
                && AfmConstant.YES.equals(s.getType())).collect(Collectors.toList());
        //未使用的服务器
        List<AfmServer> collectNoUse = afmServers.stream().filter(s -> AfmConstant.YES.equals(s.getStatus())
                && s.getCollectionName() == null).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collectNoUse)) {
            log.error("已无可分配的资源，请新建服务器");
            //预警
        }
        if (!CollectionUtils.isEmpty(collect)) {
            Map map = new HashMap();
            map.put("servers", JSON.toJSONString(collect));
            String s = JSON.toJSONString(map);
            String url = cnnUrl + "/queryServerUseSize";
            String cnnStr = CnnHttpUtil.getHttp(url, s);
            CnnHttpUtil.CnnRetHandle result = CnnHttpUtil.getCnnRetHandle(cnnStr);
            if (result.succ) {
                JSONArray objects = result.jsonObject1.getJSONArray("data");
                ArrayList<AfmServer> objects1 = new ArrayList<>();
                ArrayList<AfmServer> updates = new ArrayList<>();
                for (int i = 0; i < objects.size(); i++) {
                    switchServer(objects, i, objects1, updates);
                }
                //只创建最新日期的集合数据
                List<AfmServer> collect1 = objects1.stream().sorted(Comparator.comparing(AfmServer::getYear).reversed()).collect(Collectors.toList());
                Map<String, List<AfmServer>> collect2 = collect1.stream().collect(Collectors.groupingBy(AfmServer::getDocCode));
                Map<String, List<AfmServer>> collect3 = updates.stream().collect(Collectors.groupingBy(AfmServer::getDocCode));

                Set<Long> ids = new HashSet<>();
                List<Long> types =new ArrayList<>();
                if (!CollectionUtils.isEmpty(collectNoUse)) {
                    for (String m : collect2.keySet()) {
                        List<AfmServer> afmServers1 = collect2.get(m);
                        if (!CollectionUtils.isEmpty(afmServers1)) {
                            AfmServer afmServer = afmServers1.get(0);
                            List<AfmServer> collect4 = collectNoUse.stream().filter(w -> w.getDocCode() != null && w.getDocCode().equals(m)).collect(Collectors.toList());

                            AfmServer afmServer1 = null;
                            if (!CollectionUtils.isEmpty(collect4)) {
                                afmServer1 = collect4.get(0);
                                ids.add(afmServer1.getId());
                            } else {
                                List<AfmServer> collect5 = collectNoUse.stream().filter(w -> w.getDocCode() == null).collect(Collectors.toList());
                                if (!CollectionUtils.isEmpty(collect5)) {
                                    for (AfmServer mm : collect5) {
                                        if (!ids.contains(mm.getId())) {
                                            afmServer1 = mm;
                                            ids.add(mm.getId());
                                            break;
                                        }
                                    }
                                }

                            }
                            if (afmServer1 == null) {
                                log.error("服务器资源不够使用");
                            }else{
                                afmServer1.setType(afmServer.getType());
                                afmServer1.setYear(afmServer.getYear());
                                afmServer1.setCollectionName(afmServer.getCollectionName());
                                afmServer1.setNum(afmServer.getNum());
                                afmServer1.setDocCode(afmServer.getDocCode());
                                afmServerMapper.updateById(afmServer1);
                            }


                            if (autoSwitch == AfmConstant.AUTO_SWITCH_YES) {
                                List<AfmServer> afmServers2 = collect3.get(m);
                                if (!CollectionUtils.isEmpty(afmServers2)) {
                                    for (AfmServer server : afmServers2) {
                                        types.add(server.getId());
                                    }
                                }
                            }
                        }
                    }
                }

                if (!CollectionUtils.isEmpty(updates)) {
                    for (AfmServer server : updates) {
                        Integer type = null;
                        if(types.contains(server.getId())){
                            type= AfmConstant.NO;
                        }
                        afmServerMapper.update(null, new LambdaUpdateWrapper<AfmServer>()
                                .set(type!=null,AfmServer::getType, type)
                                .set(AfmServer::getCollectionTotal, server.getCollectionTotal())
                                .eq(AfmServer::getDocCode, server.getDocCode())
                                .eq(AfmServer::getId, server.getId()));
                    }
                }
            }
        }
    }

    /**
     * 切换服务器
     */
    @Transactional(rollbackFor = Exception.class)
    public void switchServer(JSONArray objects, int i, ArrayList<AfmServer> objects1, ArrayList<AfmServer> updates) {
        JSONObject jsonObject = objects.getJSONObject(i);
        Long total = jsonObject.getLong("num");
        JSONObject jsonObject1 = jsonObject.getJSONObject("server");
        Integer serverNum = jsonObject1.getInteger("num");
        Long id = jsonObject1.getLong("id");
        //自动切换
        //满足总使用率大于设置的阈值或者最小可用空间小于设定的最小值，则切换服务器
        String docCode = jsonObject1.getString("docCode");
        if (total > (maxSize)) {
            if (autoSwitch == AfmConstant.AUTO_SWITCH_YES) {
                Integer year = jsonObject1.getInteger("year");
                AfmServer afmServer1 = new AfmServer();
                afmServer1.setId(id);
                afmServer1.setDocCode(docCode);
                afmServer1.setCollectionTotal(total);
                updates.add(afmServer1);

                //新建服务器
                AfmServer afmServer = new AfmServer();
                afmServer.setYear(year);
                if (year.equals(year)) {
                    afmServer.setNum(serverNum + 1);
                } else {
                    afmServer.setNum(1);
                }
                afmServer.setDocCode(docCode);
                afmServer.setType(AfmConstant.YES);
                String s = getCollectName(docCode, year,  afmServer.getNum());
                String collectionName = Base64Utils.encode(s);
                afmServer.setCollectionName(collectionName);
                objects1.add(afmServer);

            } else {
                AfmServer afmServer1 = new AfmServer();
                afmServer1.setId(id);
                afmServer1.setDocCode(docCode);
                afmServer1.setCollectionTotal(total);
                updates.add(afmServer1);
                log.error("预警提示，人工切换");

            }
        } else {
            afmServerMapper.update(null, new LambdaUpdateWrapper<AfmServer>()
                    .set(AfmServer::getCollectionTotal, total)
                    .eq(AfmServer::getId, id));

        }
    }


    /**
     *
     */
    public AfmServer getCollectNameBase(String materialTypeCode, Integer year,int detType) {
        //判断是否多年内的数据放在一台服务器上
        AssertUtils.isNull(materialTypeCode, "资料类型不能为空");
        //查询当前资料当前年份的向量服务器
        AfmServer afmServer = getAfmServerBase(materialTypeCode, year,detType);

        if (afmServer == null) {
            // 当前查询条件没有对应的向量服务器
            // 判断是否当前资料有历年的向量服务器
            List<AfmServer> afmServerList = afmServerMapper.selectList(new LambdaQueryWrapper<AfmServer>()
                    .eq(AfmServer::getDocCode, materialTypeCode)
                    .eq(AfmServer::getStatus, AfmConstant.YES)
                    .eq(AfmServer::getType, AfmConstant.YES));
            if (!CollectionUtils.isEmpty(afmServerList)) {
                //当前资料存在历年的向量服务器，自动创建一条同名服务器（只有集合名称不一致，并返回新服务器数据,更新老的服务器设置可写为可读）
                List<AfmServer> collect = afmServerList.stream().sorted(Comparator.comparing(AfmServer::getYear).reversed()).collect(Collectors.toList());
                AfmServer afmServerNew = new AfmServer();
                BeanUtils.copyProperties(collect.get(0), afmServerNew);
                int num = 1;
                String s = getCollectName(materialTypeCode, year, num);
                //如果是文本查重，集合名称后面添加text
                s = AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE.equals(detType)? s + "_text" : s;
                String collectionName = Base64Utils.encode(s);
                afmServerNew.setDocCode(materialTypeCode);
                afmServerNew.setType(AfmConstant.YES);
                afmServerNew.setId(snowflakeUtil.nextId());
                afmServerNew.setYear(year);
                afmServerNew.setNum(num);
                afmServerNew.setCollectionName(collectionName);
                afmServerNew.setServerType(detType);
                afmServerMapper.insert(afmServerNew);
                return afmServerNew;
            } else {
                //资料类型不存在，所有都是新建的
                //需要重新分配向量数据库
                //只有当这个资料类型从未配置过才会出现这种情况。即初始化服务器
                List<AfmServer> afmServerCanUse = afmServerMapper.selectList(new LambdaQueryWrapper<AfmServer>()
                            .eq(AfmServer::getStatus, AfmConstant.YES)
                            .orderByDesc(AfmServer::getId));
                List<AfmServer> collect = afmServerCanUse.stream().filter(s -> s.getCollectionName() == null || s.getCollectionName().equals("")).collect(Collectors.toList());
                List<AfmServer> mater = afmServerCanUse.stream().filter(s -> materialTypeCode.equals(s.getDocCode())).collect(Collectors.toList());
                AssertUtils.notNull(mater, "当前资料类型，可写的服务器配置有误");
                if (CollectionUtils.isEmpty(collect)) {
                    AssertUtils.isTrue(true, "暂无可用服务器");
                } else if (collect.size() == 1) {
                    //预警 只剩最后一台
                }
                AfmServer afmServerNew = collect.get(0);
                afmServerNew.setDocCode(materialTypeCode);
                afmServerNew.setType(AfmConstant.YES);
                //分配服务器
                int num = 1;
                String s = getCollectName(materialTypeCode, year, num);
                //如果是文本查重，集合名称后面添加text
                s = AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE.equals(detType)? s + "_text" : s;
                String collectionName = Base64Utils.encode(s);
                afmServerNew.setYear(year);
                afmServerNew.setNum(num);
                afmServerNew.setCollectionName(collectionName);
                afmServerNew.setServerType(detType);
                afmServerMapper.updateById(afmServerNew);
                return afmServerNew;
            }
        } else {
            if (afmServer.getCollectionTotal() != null && afmServer.getCollectionTotal() * 0.9 > maxSize) {
                AssertUtils.isTrue(true, "已超过服务器可写入的上限，请先切换服务器");
            }
            return afmServer;
        }

    }

    private static String getCollectName(String materialTypeCode, Integer year, int num) {
        String transMater = materialTypeCode;
        if(materialTypeCode.contains("-")){
            transMater = transMater.replace("-","_repalace_");
        }
        String s = AfmConstant.COLLECTION_HEAD + transMater + "_" + year + "_" + num;
        return s;
    }

    private AfmServer getAfmServerBase(String materialTypeCode, Integer year,int detType) {
        AfmServer afmServer = afmServerMapper.selectOne(new LambdaQueryWrapper<AfmServer>()
                .eq(AfmServer::getYear, year)
                .eq(AfmServer::getDocCode, materialTypeCode)
                .eq(AfmServer::getStatus, AfmConstant.YES)
                .eq(AfmServer::getType, AfmConstant.YES)
                .eq(AfmServer::getServerType, detType));
        return afmServer;
    }

}
