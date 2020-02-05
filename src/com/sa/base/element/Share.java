/**
 *
 * 项目名称：NettyServer
 * 类名称：Share
 * 类描述：
 * 创建人：Y.P
 * 创建时间：2018年12月25日 下午12:52:42
 * 修改人：Y.P
 * 修改时间：2018年12月25日 下午12:52:42
 * @version  1.0
 *
 */
package com.sa.base.element;

import java.util.ArrayList;
import java.util.List;

public class Share {
	// 共享对象类型 1：属性 n:集合 2:两者都有
	private String type = "1";
	
	private Object content = null;
	
	private List<Object> listContent = null;
	
	public Share() {}

	public Share(String type) {
		this.type = type;

		if ("2".equals(this.type) || "n".equals(this.type)) {
			listContent = new ArrayList<>();
		}
	}

	/**
	 * 向集合中追加一个值
	 */
	public void add(Object obj) {
		int rs = this.ifnull();

		if (-1 == rs) {
			this.listContent = new ArrayList<>();
		}
		
		this.listContent.add(obj);
	}

	/**
	 * 修改集合中某一个值
	 */
	public int updListContent(int index, Object obj) {
		int rs = this.ifnull();

		if (-1 == rs) {
			this.listContent = new ArrayList<>();
		}
		
		this.listContent.set(index, obj);
		
		return 0;
	}

	public int updListContent(Object oldObj, Object newObj) {
		int rs = this.ifnull();

		if (-1 == rs) {
			this.listContent = new ArrayList<>();
		}
		for (int i = 0; i < this.listContent.size(); i++) {
			if(this.listContent.get(i).equals(oldObj)){
				this.listContent.set(i, newObj);
			}
		}
		return 0;
	}

	/**
	 * 移出
	 */
	public int removeListContent(int index) {
		int rs = this.ifnull();

		if (-1 == rs) {
			return rs;
		}

		this.listContent.remove(index);

		return 0;
	}
	
	/**
	 * 移出
	 */
	public int removeListContent(int index, int len) {
		int rs = this.ifnull();

		if (-1 == rs) {
			return rs;
		}
		
		if (index > this.listContent.size()-1) {
			return -2;
		}
		
		if (index + len - 1 > this.listContent.size()-1) {
			return -3;
		}
		
		if (len < 0) {
			return -3;
		}

		int end = this.listContent.size()-1;
		if (index > -1) {
			end = index + len - 1;
		}

		for (int i=end; i>=index; i--) {
			this.listContent.remove(i);
		}

		return 0;
	}

	public int removeListContent(Object obj) {
		int rs = this.ifnull();

		if (-1 == rs) {
			return rs;
		}

		this.listContent.remove(obj);
		
		return 0;
	}

	/**
	 * 清空
	 */
	public void clearContent() {
		this.setContent(null);
	}

	public void clearListContent() {
		this.setListContent(new ArrayList<>());
	}

	private int ifnull() {
		if (null == this.listContent) {
			return -1;
		}
		
		return 0;
	}

	public List<Object> getListContent() {
		return listContent;
	}

	public void setListContent(List<Object> listContent) {
		this.listContent = listContent;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
