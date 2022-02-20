package jp.yupj.kessantool;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.io.*;

public class KessanFrame {
	public static final int SCREEN_SHIWAKE = 1;
	public static final int SCREEN_KAMOKU = 2;
	public static final int SCREEN_SEISAN = 3;

	JFrame f;

	ShiwakeTableControl shiwake;
	JPanel panelshowake;

	KamokuTableControl kamoku;
	SeisanTableControl seisan;

	KessanData data;
	int curScreen = 0;

	public KessanFrame(){
		shiwake = new ShiwakeTableControl();
		kamoku = new KamokuTableControl();
		seisan = new SeisanTableControl();
	}

	public void setData(KessanData data){
		this.data = data;
		startWhenPrepareFinish();
	}

	public void startWhenPrepareFinish(){
		if (curScreen == 0 && f != null && data != null){
			setScreen(SCREEN_SEISAN);
		}
	}


	public void open(){
		f = new JFrame();
		JMenuBar bar = new JMenuBar();
		JMenu m = new JMenu("メニュー");
		bar.add(m);
		JMenuItem mi = new JMenuItem("仕訳表");
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){
				menuDo("shiwake");
			}
		});
		m.add(mi);
		mi = new JMenuItem("精算表");
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){
				menuDo("seisan");
			}
		});
		m.add(mi);
		mi = new JMenuItem("勘定科目ごと");
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){
				menuDo("kamoku");
			}
		});
		m.add(mi);
		mi = new JMenuItem("貸借対照表の書き出し");
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){
				menuDo("taishakuout");
			}
		});
		m.add(mi);
		f.setJMenuBar(bar);


		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 500, 500);
		f.setVisible(true);
		startWhenPrepareFinish();


	}
	public void menuDo(String code){
		if (code.equals("shiwake")){
			setScreen(SCREEN_SHIWAKE);
		}
		else if (code.equals("seisan")){
			setScreen(SCREEN_SEISAN);
		}
		else if (code.equals("kamoku")){
			setScreen(SCREEN_KAMOKU);
		}
		else if (code.equals("taishakuout")){
			doTaishakuOutput();
		}
	}

	public void doTaishakuOutput(){
		if (this.data == null) return;
		JFileChooser chooser = new JFileChooser();
		chooser.setSelectedFile(new File(this.data.loadpath+".taishaku.txt"));
		int ans = chooser.showSaveDialog(f);
		if (ans != JFileChooser.APPROVE_OPTION) return;
		
		this.data.saveTaishakuData(chooser.getSelectedFile());
	}


	public void setScreen(int screen){
		if (screen == curScreen) return;
		if (curScreen == SCREEN_SHIWAKE){
			f.getContentPane().remove(shiwake.getPanel());
		}
		else if (curScreen == SCREEN_KAMOKU){
			f.getContentPane().remove(kamoku.getListPanel());
			f.getContentPane().remove(kamoku.getTablePanel());
		}
		else if (curScreen == SCREEN_SEISAN){
			f.getContentPane().remove(seisan.getPanel());
		}
		f.repaint();
		f.revalidate();
		curScreen = screen;
		if (screen == SCREEN_SHIWAKE){
			f.setTitle("仕訳帳");
			f.getContentPane().add(shiwake.getPanel());
			shiwake.rebuild(data);
		}
		else if (screen == SCREEN_KAMOKU){
			f.setTitle("勘定科目ごと");
			f.getContentPane().add(BorderLayout.EAST, kamoku.getListPanel());
			f.getContentPane().add(BorderLayout.CENTER, kamoku.getTablePanel());
			kamoku.rebuild(data);
		}
		else if (screen == SCREEN_SEISAN){
			f.setTitle("精算表");
			f.getContentPane().add(seisan.getPanel());
			seisan.rebuild(data);
		}
		f.repaint();
		f.revalidate();

	}



	class ShiwakeTableControl extends AbstractKessanTableControl{
		public String headerText(int i){
			if (i == 0) return "日付";
			if (i == 1) return "取引先";
			if (i == 2) return "借方勘定";
			if (i == 3) return "借方金額";
			if (i == 4) return "貸方勘定";
			if (i == 5) return "貸方金額";
			if (i == 6) return "備考";
			return "";
		}

		public void rendererSet(KessanTableRenderer renderer, Object value, int row, int column, boolean forHeader){
			if (!forHeader){
				Color textcolor = Color.BLACK;
				if (column == 2 || column == 4){
					int kamoku = KessanConfig.getKamoku(value.toString());
					textcolor = KessanConfig.getColor(kamoku);
				}
				renderer.setForeground(textcolor);
				if (column == 3 || column == 5){
					renderer.setHorizontalAlignment(SwingConstants.RIGHT);
				}
				else{
					renderer.setHorizontalAlignment(SwingConstants.LEFT);
				}
			}
		}

		public int getRowCount(){
			if (this.data == null) return 1;
			return this.data.count()+1;
		}
		public int getColumnCount(){
			return 7;
		}
		public Object getValueAt(int row, int col){
			if (this.data == null) return "";
			if (row < this.data.count()){
				IKessanLine l = this.data.get(row);
				if (col == 0) return l.getHiduke();
				if (col == 1) return l.getTorihikisaki();
				if (col == 2) return l.getKariKamoku();
				if (col == 3) return (l.getKariKingaku()==0)?"":""+l.getKariKingaku();
				if (col == 4) return l.getKashiKamoku();
				if (col == 5) return (l.getKashiKingaku()==0)?"":""+l.getKashiKingaku();
				if (col == 6) return ""+l.getBikou();
			}
			else{
				if (col == 1) return "合計";
				if (col == 3) return ""+this.data.shiwakeKariTotal();
				if (col == 5) return ""+this.data.shiwakeKashiTotal();
				if (col == 6 && this.data.isShiwakeError()) return "ERROR";
			}
			return "";
		}

	}

	class KamokuTableControl extends AbstractKessanTableControl implements ListSelectionListener{
		JList<KessanData.KamokuData> list;
		JScrollPane scl;
		KamokuListModel listmodel;
		KessanData.KamokuData current;
		KamokuListRenderer listRenderer;

		public KamokuTableControl(){
			super();
			super.dataDirectFlag = false;
			listmodel = new KamokuListModel();
			list = new JList<KessanData.KamokuData>(listmodel);
			list.setCellRenderer(new KamokuListRenderer());
			list.addListSelectionListener(this);
			scl = new JScrollPane(list);
		}

		public String headerText(int i){
			if (i == 0) return "日付";
			if (i == 1) return "取引先";
			if (i == 2) return "借方金額";
			if (i == 3) return "取引先";
			if (i == 4) return "貸方金額";
			if (i == 5) return "備考";
			return "";
		}

		public void valueChanged(ListSelectionEvent e){
			current = listmodel.getElementAt(list.getSelectedIndex());
			f.setTitle("勘定科目ごと："+current.kamoku);
			tableReset();
		}


		public void afterRebuild(){
			if (listmodel.getSize() > 0){
				list.setSelectedIndex(0);
				current = listmodel.getElementAt(0);
				tableReset();
			}
		}

		public void rendererSet(KessanTableRenderer renderer, Object value, int row, int column, boolean forHeader){
			if (!forHeader){
				if (column == 2 || column == 4){
					renderer.setHorizontalAlignment(SwingConstants.RIGHT);
				}
				else{
					renderer.setHorizontalAlignment(SwingConstants.LEFT);
				}
			}
		}


		public JComponent getListPanel(){
			return scl;
		}
		public JComponent getTablePanel(){
			return getPanel();
		}


		public int getColumnCount(){
			return 6;
		}
		public int getRowCount(){
			if (current == null) return 1;
			return current.count()+1;
		}
		public Object getValueAt(int row, int col){
			if (current == null) return "";
			if (row == current.count()){
				if (col == 0) return "";
				else if (col == 1 || col == 3)  return "合計";
				else if (col == 2) return ""+current.kariTotal();
				else if (col == 4) return ""+current.kashiTotal();
			}
			else{
				IKessanLine l = current.get(row);
				if (col == 0) return l.getHiduke();
				else if (col == 1) {
					if (current.kamoku.equals(l.getKariKamoku())) return l.getTorihikisaki();
				}
				else if (col == 2) {
					if (current.kamoku.equals(l.getKariKamoku())) return ""+l.getKariKingaku();
				}
				else if (col == 3) {
					if (current.kamoku.equals(l.getKashiKamoku())) return l.getTorihikisaki();
				}
				else if (col == 4) {
					if (current.kamoku.equals(l.getKashiKamoku())) return ""+l.getKashiKingaku();
				}
				else if (col == 5) {
					return ""+l.getBikou();
				}
			}
			return "";
		}
		class KamokuListRenderer extends DefaultListCellRenderer{
			Font usefont;
			EmptyBorder padding;
			public KamokuListRenderer(){
				super();
				padding = new EmptyBorder(3,2, 3, 2);
				try{
					usefont =  KessanConfig.getFont(14);
				}
				catch(Exception e){
				}
			}
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){
				super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
				setFont(usefont);
				setBorder(padding);
				setVerticalAlignment(SwingConstants.CENTER);
				Color col = Color.black;
				col = KessanConfig.getColor(((KessanData.KamokuData)value).type);
				setForeground(col);
				return this;
			}

		}

		class KamokuListModel extends DefaultListModel<KessanData.KamokuData>{
			public int getSize(){
				System.out.println(data.kamokuCount());
				return data.kamokuCount();
			}
			public KessanData.KamokuData getElementAt(int i){
				System.out.println(data.getKamokuData(i));
				return data.getKamokuData(i);
			}
		}
	}


	class SeisanTableControl extends AbstractKessanTableControl{

		public void rendererSet(KessanTableRenderer renderer, Object value, int row, int column, boolean forHeader){
			if (!forHeader){
				if (column > 0){
					renderer.setHorizontalAlignment(SwingConstants.RIGHT);
				}
				else{
					renderer.setHorizontalAlignment(SwingConstants.LEFT);
				}
				Color col = Color.BLACK;
				if (column == 0){
					if (row < getRowCount()-2){
						col = KessanConfig.getColor(KessanConfig.getKamoku((String)value));
					}
				}
				renderer.setForeground(col);
			}
		}

		public String headerText(int i){
			if (i == 0) return "勘定科目";
			else if (i == 1) return "残高（借方）";
			else if (i == 2) return "残高（貸方）";
			else if (i == 3) return "修正（借方）";
			else if (i == 4) return "修正（貸方）";
			else if (i == 5) return "損益（借方）";
			else if (i == 6) return "損益（貸方）";
			else if (i == 7) return "貸借（借方）";
			else if (i == 8) return "貸借（貸方）";
			return "";
		}
		public int getColumnCount(){
			return 9;
		}
		public int getRowCount(){
			if (this.data == null) return 1;
			return this.data.kamokuCount()+2;
		}
		public Object getValueAt(int row, int column){
			if (this.data == null) return "";
			if (row == this.data.kamokuCount()+1){
				if (column == 0) return "合計";
				if (column == 1) return ""+this.data.zandakaKariTotal;
				if (column == 2) return ""+this.data.zandakaKashiTotal;
				if (column == 3) return ""+this.data.shuseiKariTotal;
				if (column == 4) return ""+this.data.shuseiKashiTotal;
				if (column == 5) return ""+this.data.sonekiKariTotal;
				if (column == 6) return ""+this.data.sonekiKashiTotal;
				if (column == 7) return ""+this.data.taishakuKariTotal;
				if (column == 8) return ""+this.data.taishakuKashiTotal;

			}
			else if (row == this.data.kamokuCount()){
				if (column == 0) return "当期純利益";
				if (column == 5 || (column == 8 && !this.data.junriekiFurikae)) if (this.data.toukiJunrieki > 0) return ""+this.data.toukiJunrieki;
				if (column == 6 || (column == 7 && !this.data.junriekiFurikae)) if (this.data.toukiJunrieki < 0) return ""+(-this.data.toukiJunrieki);
			}
			else {
				KessanData.KamokuData kamoku  = this.data.getKamokuData(row);
				if (column == 0) return kamoku.kamoku;
				else if (column == 1){
					if (kamoku.kariTotal() > kamoku.kashiTotal()){
						return kamoku.kariTotal() - kamoku.kashiTotal();
					}
				}
				else if (column == 2){
					if (kamoku.kariTotal() < kamoku.kashiTotal()){
						return kamoku.kashiTotal() - kamoku.kariTotal();
					}
				}
				else if (column == 5){
					if (kamoku.type == KessanConfig.SHUEKI || kamoku.type == KessanConfig.HIYOU){
						if (kamoku.kariTotal() > kamoku.kashiTotal()){
							return kamoku.kariTotal() - kamoku.kashiTotal();
						}
					}
				}
				else if (column == 6){
					if (kamoku.type == KessanConfig.SHUEKI || kamoku.type == KessanConfig.HIYOU){
						if (kamoku.kariTotal() < kamoku.kashiTotal()){
							return kamoku.kashiTotal() - kamoku.kariTotal();
						}
					}
				}
				else if (column == 7){
					if (kamoku.type == KessanConfig.SHISAN || kamoku.type == KessanConfig.FUSAI || kamoku.type == KessanConfig.JUNSHISAN){
						if (kamoku.kariTotalSime() > kamoku.kashiTotalSime()){
							return kamoku.kariTotalSime() - kamoku.kashiTotalSime();
						}
					}
				}
				else if (column == 8){
					if (kamoku.type == KessanConfig.SHISAN || kamoku.type == KessanConfig.FUSAI || kamoku.type == KessanConfig.JUNSHISAN){
						if (kamoku.kariTotalSime() < kamoku.kashiTotalSime()){
							return kamoku.kashiTotalSime() - kamoku.kariTotalSime();
						}
					}
				}
			}
			return "";
		}
	}

	class ShiwakeTableControlx extends AbstractTableModel{
		ShiwakeRenderer renderer;
		ShiwakeRenderer rendererHeader;
		JTable table;
		JScrollPane sc;
		KessanData data;
		ArrayList<Dimension> szlist;

		public ShiwakeTableControlx(){
			table = new JTable(this);
			table.getColumnModel().setColumnMargin(0);
			System.out.println("component count="+table.getComponentCount());
			System.out.println("component="+table.getComponent(0));
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			sc = new JScrollPane(table);
			renderer = new ShiwakeRenderer();
			rendererHeader = new ShiwakeRenderer(true);
			szlist = new ArrayList<Dimension>();
//			Font nfont = new Font("Hiragino Sans", Font.PLAIN, 16).deriveFont(22.0f);
//			table.setFont(nfont);
		}
		public JComponent getPanel(){
			return sc;
		}
		public void rebuild(KessanData data){
			this.data = data;
			prepareRenderer();
			fireTableDataChanged();
			fit();
		}
		protected void prepareRenderer(){
			Insets padding = renderer.getPadding();
			for(int i = 0; i < getColumnCount(); i++){
				table.getColumnModel().getColumn(i).setCellRenderer(renderer);
				table.getColumnModel().getColumn(i).setHeaderRenderer(rendererHeader);
				table.getColumnModel().getColumn(i).setHeaderValue(headerText(i));
			}
			int sz = getColumnCount()*getRowCount();
			while (szlist.size() > sz){
				szlist.remove(sz);
			}
			for(int i = 0; i < sz; i++){
				Dimension d = null;
				if (i < szlist.size()){
					d = szlist.get(i);
				}
				else{
					d = new Dimension();
					szlist.add(d);
				}
				int row = i / getColumnCount();
				int col = i % getColumnCount();
				Rectangle2D rect = renderer.useFont().getStringBounds(getValueAt(row,col).toString(), table.getFontMetrics(renderer.useFont()).getFontRenderContext());
				d.width = (int)rect.getWidth()+padding.left+padding.right+2;
				d.height = (int)rect.getHeight()+padding.top+padding.bottom+2;
			}
		}

		public String headerText(int i){
			if (i == 0) return "日付";
			if (i == 1) return "取引先";
			if (i == 2) return "借方勘定";
			if (i == 3) return "借方金額";
			if (i == 4) return "貸方勘定";
			if (i == 5) return "貸方金額";
			if (i == 6) return "備考";
			return "";
		}

		class ShiwakeRenderer extends DefaultTableCellRenderer{
			boolean forHeader = false;
			Font usefont;
			EmptyBorder padding;
			Border lineborder;
			public ShiwakeRenderer(){
				this(false);
			}
			public Insets getPadding(){
				return padding.getBorderInsets();
			}
			public ShiwakeRenderer(boolean forHeader){
				this.forHeader = forHeader;
				setVerticalAlignment(SwingConstants.CENTER);
				try{
					if (forHeader){
	//					usefont = new Font("Hiragino Sans", Font.PLAIN, 16).deriveFont(22.0f);
						usefont = Font.createFont(Font.TRUETYPE_FONT, new File("/System/Library/Fonts/ヒラギノ角ゴシック W6.ttc")).deriveFont(14.0f);
//	//					usefont = Font.getFont("Serif").deriveFont(14.0f).deriveFont(Font.BOLD);
					}
					else{
	//					usefont = new Font("Hiragino Sans", Font.PLAIN, 16).deriveFont(22.0f);
						usefont = Font.createFont(Font.TRUETYPE_FONT, new File("/System/Library/Fonts/ヒラギノ角ゴシック W3.ttc")).deriveFont(16.0f);
//	//					usefont = Font.getFont("Serif").deriveFont(16.0f);
					}
				}
				catch(Exception e){
				}
				padding = new EmptyBorder(3, 6, 3, 6);
				lineborder = new CompoundBorder(new MatteBorder(1, 0, 0, 0, Color.BLACK), padding);
			}
			public Font useFont(){
				return usefont;
			}
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				setFont(usefont);
				setBorder((row < getRowCount()-1)?padding:lineborder);
				if (forHeader){
					setHorizontalAlignment(SwingConstants.CENTER);
					setBackground(Color.LIGHT_GRAY);
				}
				else{
					Color textcolor = Color.BLACK;
					if (column == 2 || column == 4){
						int kamoku = KessanConfig.getKamoku(getText());
						textcolor = KessanConfig.getColor(kamoku);
					}
					setForeground(textcolor);
					if (column == 3 || column == 5){
						setHorizontalAlignment(SwingConstants.RIGHT);
					}
					else{
						setHorizontalAlignment(SwingConstants.LEFT);
					}
				}
				return this;
			}
		}
		public void fit(){
			for(int i = 0; i < getRowCount(); i++){
				int preh = 10;
				for(int j = 0; j < getColumnCount(); j++){
					Dimension d = szlist.get(i*getColumnCount()+j);
					int h = d.height;
					if (preh < h) preh = h;
				}
				table.setRowHeight(i, preh);
			}
			for(int i = 0; i < getColumnCount(); i++){
				TableColumn col = table.getColumnModel().getColumn(i);
				int prew = 10;
				for(int j = 0; j < getRowCount(); j++){
					Dimension d = szlist.get(j*getColumnCount()+i);
					int w = d.width;
					if (prew < w) prew = w;
				}
				col.setPreferredWidth(prew+2);
			}
		}

		public int getRowCount(){
			if (this.data == null) return 1;
			return this.data.count()+1;
		}
		public int getColumnCount(){
			return 7;
		}
		public Object getValueAt(int row, int col){
			if (this.data == null) return "";
			if (row < this.data.count()){
				IKessanLine l = this.data.get(row);
				if (col == 0) return l.getHiduke();
				if (col == 1) return l.getTorihikisaki();
				if (col == 2) return l.getKariKamoku();
				if (col == 3) return (l.getKariKingaku()==0)?"":""+l.getKariKingaku();
				if (col == 4) return l.getKashiKamoku();
				if (col == 5) return (l.getKashiKingaku()==0)?"":""+l.getKashiKingaku();
				if (col == 6) return ""+l.getBikou();
			}
			else{
				if (col == 1) return "合計";
				if (col == 3) return ""+this.data.shiwakeKariTotal();
				if (col == 5) return ""+this.data.shiwakeKashiTotal();
				if (col == 6 && this.data.isShiwakeError()) return "ERROR";
			}
			return "";
		}

	}





	class KamokuTableControlx extends AbstractListModel<KessanData.KamokuData>{
		JScrollPane sc;
		JTable table;
		JScrollPane scl;
		JList<KessanData.KamokuData> list;
		KessanData data;
		KamokuListRenderer listRenderer;
		KamokuTableModel model;
		KamokuTableRenderer tableRenderer;

		public KamokuTableControlx(){
			model = new KamokuTableModel();
			tableRenderer = new KamokuTableRenderer();
			table = new JTable(model);
			sc = new JScrollPane(table);
			list = new JList<KessanData.KamokuData>();
			list.setModel(this);
			listRenderer = new KamokuListRenderer();
			list.setCellRenderer(listRenderer);
			scl = new JScrollPane(list);
		}

		public int getSize(){
			System.out.println(data.kamokuCount());
			return data.kamokuCount();
		}
		public KessanData.KamokuData getElementAt(int i){
			System.out.println(data.getKamokuData(i));
			return data.getKamokuData(i);
		}

		
		class KamokuListRenderer extends DefaultListCellRenderer{
			Font usefont;
			EmptyBorder padding;
			public KamokuListRenderer(){
				super();
				padding = new EmptyBorder(3,2, 3, 2);
				try{
					usefont =  KessanConfig.getFont(14);
				}
				catch(Exception e){
				}
			}
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){
				super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
				setFont(usefont);
				setBorder(padding);
				setVerticalAlignment(SwingConstants.CENTER);
				Color col = Color.black;
				col = KessanConfig.getColor(((KessanData.KamokuData)value).type);
				setForeground(col);
				return this;
			}

		}

		public JComponent getListPanel(){
			return scl;
		}
		public JComponent getTablePanel(){
			return sc;
		}

		public void rebuild(KessanData data){
			this.data = data;
			model.current = this.data.getKamokuData(0);
		}

		class KamokuTableModel extends AbstractTableModel{
			KessanData.KamokuData current;
			public int getColumnCount(){
				return 5;
			}
			public int getRowCount(){
				return current.count();
			}
			public Object getValueAt(int row, int col){
				IKessanLine l = current.get(row);
				if (col == 0) return l.getHiduke();
				else if (col == 1) {
					if (current.kamoku.equals(l.getKashiKamoku())) return l.getTorihikisaki();
				}
				else if (col == 2) {
					if (current.kamoku.equals(l.getKashiKamoku())) return ""+l.getKashiKingaku();
				}
				else if (col == 3) {
					if (current.kamoku.equals(l.getKariKamoku())) return l.getTorihikisaki();
				}
				else if (col == 4) {
					if (current.kamoku.equals(l.getKariKamoku())) return ""+l.getKariKingaku();
				}
				return "";
			}
		}
		class KamokuTableRenderer extends DefaultTableCellRenderer{

		}
	}


	abstract class AbstractKessanTableControl extends AbstractTableModel{
		KessanTableRenderer renderer;
		KessanTableRenderer rendererHeader;
		JTable table;
		JScrollPane sc;
		KessanData data;
		ArrayList<Dimension> szlist;
		boolean dataDirectFlag=true;

		public AbstractKessanTableControl(){
			table = new JTable(this);
			table.getColumnModel().setColumnMargin(0);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			sc = new JScrollPane(table);
			renderer = new KessanTableRenderer();
			rendererHeader = new KessanTableRenderer(true);
			szlist = new ArrayList<Dimension>();
		}
		public JComponent getPanel(){
			return sc;
		}
		public void rebuild(KessanData data){
			this.data = data;
			if (dataDirectFlag){
				tableReset();
			}
			else{
				afterRebuild();
			}
		}
		public void afterRebuild(){
		}
		public void tableReset(){
			prepareRenderer();
			fireTableDataChanged();
			fit();
		}
		protected void prepareRenderer(){
			Insets padding = renderer.getPadding();
			for(int i = 0; i < getColumnCount(); i++){
				table.getColumnModel().getColumn(i).setCellRenderer(renderer);
				table.getColumnModel().getColumn(i).setHeaderRenderer(rendererHeader);
				table.getColumnModel().getColumn(i).setHeaderValue(headerText(i));
			}
			int sz = getColumnCount()*(getRowCount()+1);
			while (szlist.size() > sz){
				szlist.remove(sz);
			}
			for(int i = 0; i < sz; i++){
				Dimension d = null;
				if (i < szlist.size()){
					d = szlist.get(i);
				}
				else{
					d = new Dimension();
					szlist.add(d);
				}
				int row = i / getColumnCount();
				int col = i % getColumnCount();
				Rectangle2D rect;
				if (row == 0){
					rect = rendererHeader.useFont().getStringBounds(headerText(col).toString(), table.getFontMetrics(rendererHeader.useFont()).getFontRenderContext());
				}
				else{
					rect = renderer.useFont().getStringBounds(getValueAt(row-1,col).toString(), table.getFontMetrics(renderer.useFont()).getFontRenderContext());
				}
				d.width = (int)rect.getWidth()+padding.left+padding.right+2;
				d.height = (int)rect.getHeight()+padding.top+padding.bottom+2;
			}
		}
		public void fit(){
			for(int i = 0; i < getRowCount(); i++){
				int preh = 10;
				for(int j = 0; j < getColumnCount(); j++){
					Dimension d = szlist.get((i+1)*getColumnCount()+j);
					int h = d.height;
					if (preh < h) preh = h;
				}
				table.setRowHeight(i, preh);
			}
			for(int i = 0; i < getColumnCount(); i++){
				TableColumn col = table.getColumnModel().getColumn(i);
				int prew = 10;
				for(int j = 0; j < getRowCount()+1; j++){
					Dimension d = szlist.get(j*getColumnCount()+i);
					int w = d.width;
					if (prew < w) prew = w;
				}
				col.setPreferredWidth(prew+2);
			}
		}
		public String headerText(int i){
			return "";
		}
		

		public void rendererSet(KessanTableRenderer renderer, Object value, int row, int column, boolean forHeader){
		}
		public boolean rendererIsBorder(int row, int column, boolean forHeader){
			return ((!forHeader) && (row == getRowCount()-1));
		}

		class KessanTableRenderer extends DefaultTableCellRenderer{
			boolean forHeader = false;
			Font usefont;
			EmptyBorder padding;
			Border lineborder;
			public KessanTableRenderer(){
				this(false);
			}
			public Insets getPadding(){
				return padding.getBorderInsets();
			}
			public KessanTableRenderer(boolean forHeader){
				this.forHeader = forHeader;
				setVerticalAlignment(SwingConstants.CENTER);
				if (forHeader){
					usefont = KessanConfig.getFontBold(14.0f);
				}
				else{
					usefont = KessanConfig.getFont(16.0f);
				}
				padding = new EmptyBorder(3, 6, 3, 6);
				lineborder = new CompoundBorder(new MatteBorder(1, 0, 0, 0, Color.BLACK), padding);
			}
			public Font useFont(){
				return usefont;
			}
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				setFont(usefont);
				setBorder((rendererIsBorder(row, column, forHeader))?lineborder:padding);
				if (forHeader){
					setHorizontalAlignment(SwingConstants.CENTER);
					setBackground(Color.LIGHT_GRAY);
				}
				rendererSet(this, value, row, column, forHeader);
				return this;
			}
		}

	}
}