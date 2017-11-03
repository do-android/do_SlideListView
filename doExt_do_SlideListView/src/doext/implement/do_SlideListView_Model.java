package doext.implement;

import doext.define.do_SlideListView_MAbstract;

/**
 * 自定义扩展组件Model实现，继承do_SlideListView_MAbstract抽象类；
 *
 */
public class do_SlideListView_Model extends do_SlideListView_MAbstract {

	public do_SlideListView_Model() throws Exception {
		super();
	}
	
	@Override
	public void didLoadView() throws Exception {
		super.didLoadView();
		((do_SlideListView_View)this.getCurrentUIModuleView()).loadDefalutScriptFile();
	}
	
}
