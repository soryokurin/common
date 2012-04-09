package org.bclove.common.utils.text;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeList;

/**
 * <pre>
 * 1. 转换成简体
 * 2. 转换全角到半角
 * 3. 过滤acsii
 * </pre>
 */
public class CompositeExtractText {

    public String getText(String src) {
        if (src == null) {
            return src;
        }
        return CharNormalization.compositeTextConvert(src, false,false, true, true, false,true,false,false);
    }
    
    public String getJapaneseText(String src){
    	if(src==null){
    		return src;
    	}
    	return CharNormalization.compositeTextConvert(src, false, false, true, true, false, true, false, false);
    }
    
    public String getTextFromHtml(String src){
    	if(src==null){
    		return src;
    	}
    	/**
		 * 提取所有的text(非tag)
		 */
		Parser parser = Parser.createParser(src, "utf-8");	//初始化解析器
		NodeList nodes;
		try{
			nodes = parser.extractAllNodesThatMatch(new NodeClassFilter(TextNode.class));//提取所有的TextNode
		}
		catch(Exception e){
			return null;
		}
		StringBuffer text = new StringBuffer(); 
		TextNode textNode = null;
		Node[] nodeArray = nodes.toNodeArray();
		for(int i=0;i<nodeArray.length;i++){	//遍历所有TextNode
			textNode = (TextNode)nodeArray[i];	//合并所有的TextNode
			text.append(textNode.getText());
		}
		return text.toString();
    }

}
