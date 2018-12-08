package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service(interfaceClass = ItemSearchService.class)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    SolrTemplate solrTemplate;
    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {

      Map<String,Object> resultMap= new HashMap<String,Object>();

      //处理关键字中的空格
        if(!StringUtils.isEmpty(searchMap.get("keywords"))){
            searchMap.put("keywords",searchMap.get("keywords").toString().replaceAll(" ",""));
        }



        // 创建查询对象
        // SimpleQuery query = new SimpleQuery();
        SimpleHighlightQuery query=new SimpleHighlightQuery();
        Criteria criteria = new Criteria("item_title").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置高亮的配置信息
        HighlightOptions highlightOptions = new HighlightOptions();
        //高亮的域名
        highlightOptions.addField("item_title");
        //设置高亮的起始标签
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //设置高亮的结束标签
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);
        //查询


        HighlightPage<TbItem> itemHightlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
        // 处理高亮标题
        List<HighlightEntry<TbItem>> highlighted = itemHightlightPage.getHighlighted();
        if(highlighted!= null && highlighted.size()>0){
            for(HighlightEntry<TbItem> entry:highlighted){
                List<HighlightEntry.Highlight> highlights = entry.getHighlights();
                if (highlights != null && highlights.size() > 0 &&
                        highlights.get(0).getSnipplets() != null){
                    // 设置高亮标题
                    entry.getEntity().setTitle(highlights.get(0).getSnipplets().get(0));
                }
            }
        }
            // 设置返回列表
        resultMap.put("rows", itemHightlightPage.getContent());
        return resultMap;

    }
}
