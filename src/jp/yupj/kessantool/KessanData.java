package jp.yupj.kessantool;

import java.io.*;
import java.util.*;


public class KessanData{

	KessanLine header;
	ArrayList<KessanLine> lines;
	int shiwakeKashiTotalVal = 0;
	int shiwakeKariTotalVal = 0;

	int zandakaKariTotal = 0;
	int zandakaKashiTotal = 0;
	int shuseiKariTotal = 0;
	int shuseiKashiTotal = 0;
	int sonekiKariTotal = 0;
	int sonekiKashiTotal = 0;
	int taishakuKariTotal = 0;
	int taishakuKashiTotal = 0;
	int toukiJunrieki = 0;
	boolean junriekiFurikae = false;
	String loadpath;
	String taishakuData;

	ListComp listcomp = new ListComp();

	Hashtable<String,KamokuData> zenki = new Hashtable<String,KamokuData>();
	ArrayList<KamokuData> zenkilist = new ArrayList<KamokuData>();
	

	public KamokuData getZenkiKamokuData(String kamoku, boolean withcreate){
		KamokuData k = zenki.get(kamoku);
		if (k == null && withcreate){
			k = new KamokuData(kamoku, 0);
			zenki.put(kamoku, k);
		}
		return k;
	}


	public void dataClear(){
		lines = new ArrayList<KessanLine>();
		shiwakeKashiTotalVal = 0;
		shiwakeKariTotalVal = 0;
		zandakaKariTotal = 0;
		zandakaKashiTotal = 0;
		shuseiKariTotal = 0;
		shuseiKashiTotal = 0;
		sonekiKariTotal = 0;
		sonekiKashiTotal = 0;
		taishakuKariTotal = 0;
		taishakuKashiTotal = 0;
		toukiJunrieki = 0;
	}
	public void fix(){
		shiwakeKashiTotalVal = 0;
		shiwakeKariTotalVal = 0;
		zandakaKariTotal = 0;
		zandakaKashiTotal = 0;
		shuseiKariTotal = 0;
		shuseiKashiTotal = 0;
		sonekiKariTotal = 0;
		sonekiKashiTotal = 0;
		taishakuKariTotal = 0;
		taishakuKashiTotal = 0;
		toukiJunrieki = 0;
		junriekiFurikae = false;

		for(int i = 0; i < lines.size(); i++){
			KessanLine l = lines.get(i);
			shiwakeKashiTotalVal += l.getKashiKingaku();
			shiwakeKariTotalVal += l.getKariKingaku();

			String[] kamokus = {l.getKashiKamoku(), l.getKariKamoku()};
			for(int j = 0; j < 2; j++){
				if (kamokus[j].length() == 0) continue;
				KamokuData k = getZenkiKamokuData(kamokus[j], true);
				k.add(l);
			}
		}
		
		zenkilist.addAll(zenki.values());
		for(int i = 0; i < zenkilist.size(); i++){
			if (zenkilist.get(i).type == KessanConfig.SHUEKI || zenkilist.get(i).type == KessanConfig.HIYOU){
				zenkilist.get(i).fix();
				if (zenkilist.get(i).kariTotal() > zenkilist.get(i).kashiTotal()){
					zandakaKariTotal += zenkilist.get(i).kariTotal()-zenkilist.get(i).kashiTotal();
				}
				else if (zenkilist.get(i).kariTotal() < zenkilist.get(i).kashiTotal()){
					zandakaKashiTotal += zenkilist.get(i).kashiTotal()-zenkilist.get(i).kariTotal();
				}
				if (zenkilist.get(i).kariTotalSime() > zenkilist.get(i).kashiTotalSime()){
					sonekiKariTotal += zenkilist.get(i).kariTotalSime()-zenkilist.get(i).kashiTotalSime();
				}
				else if (zenkilist.get(i).kariTotalSime() < zenkilist.get(i).kashiTotalSime()){
					sonekiKashiTotal += zenkilist.get(i).kashiTotalSime()-zenkilist.get(i).kariTotalSime();
				}
			}
		}
		toukiJunrieki = sonekiKashiTotal-sonekiKariTotal;

		if (junriekiFurikae){
			KamokuData motoire = getZenkiKamokuData("元入金", true);
			motoire.add(new KurikoshiLine("12/31", "元入金", toukiJunrieki, true));
		}

		if (toukiJunrieki > 0){
			sonekiKariTotal += toukiJunrieki;
		}
		if (toukiJunrieki < 0){
			sonekiKashiTotal -= toukiJunrieki;
		}


		for(int i = 0; i < zenkilist.size(); i++){
			if (zenkilist.get(i).type == KessanConfig.SHISAN || zenkilist.get(i).type == KessanConfig.FUSAI || zenkilist.get(i).type == KessanConfig.JUNSHISAN){
				zenkilist.get(i).fix();
				if (zenkilist.get(i).kariTotal() > zenkilist.get(i).kashiTotal()){
					zandakaKariTotal += zenkilist.get(i).kariTotal()-zenkilist.get(i).kashiTotal();
				}
				else if (zenkilist.get(i).kariTotal() < zenkilist.get(i).kashiTotal()){
					zandakaKashiTotal += zenkilist.get(i).kashiTotal()-zenkilist.get(i).kariTotal();
				}
				if (zenkilist.get(i).kariTotalSime() > zenkilist.get(i).kashiTotalSime()){
					taishakuKariTotal += zenkilist.get(i).kariTotalSime()-zenkilist.get(i).kashiTotalSime();
				}
				else if (zenkilist.get(i).kariTotal() < zenkilist.get(i).kashiTotal()){
					taishakuKashiTotal += zenkilist.get(i).kashiTotalSime()-zenkilist.get(i).kariTotalSime();
				}
			}
		}

		if (!junriekiFurikae){
			if (toukiJunrieki > 0){
				taishakuKashiTotal += toukiJunrieki;
			}
			if (toukiJunrieki < 0){
				taishakuKariTotal -= toukiJunrieki;
			}
		}
		zenkilist.sort(listcomp);
	}
/*
	public void load(String file){
		dataClear();

		header = null;
		try{
			KessanLine owner = null;
			File f = new File(file);
			if (!f.canRead()){
				throw new Exception("File Error");
			}
			FileInputStream st = new FileInputStream(f);
			ByteArrayOutputStream bst = new ByteArrayOutputStream();
			ArrayList<String> linebase = new ArrayList<String>();
			while(true){
				int c = st.read();
				if (c == '\t' || c == '\r' || c == '\n' || c == -1){
					byte[] b = bst.toByteArray();
					bst.reset();
					if (c == -1 && b.length == 0 && linebase.size() == 0){
						break;
					}
					String v = new String(b);
					linebase.add(v);
					if (c == '\r' || c == '\n' || c == -1){
						if (linebase.get(0).length() > 0){
							owner = null;
						}
						KessanLine ln = new KessanLine(linebase, owner);
						if (owner == null){
							owner = ln;
						}
						if (this.header == null) this.header = ln;
						else lines.add(ln);
						linebase.clear();
						if (c == '\r'){
							int c2 = st.read();
							if (c2 != '\n'){
								st.skip(-1);
							}
						}
						if (c == -1) break;
					}
					continue;
				}
				bst.write(c);
			}
			dataRebuild();
		}
		catch(Exception e){
		}
	}
*/
	public void buildTaishakuData(){
		if (this.taishakuData != null) return;
		StringBuffer buf = new StringBuffer();
		buf.append("資産\t金閣\t負債・純資産\t金額\n");
		ArrayList<ArrayList<Integer>> idxs = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < 3; i++){
			idxs.add(new ArrayList<Integer>());
		};
		for(int i = 0; i < zenkilist.size(); i++){
			KamokuData k = zenkilist.get(i);
			if (k.type == KessanConfig.SHISAN){
				idxs.get(0).add(i);
			}
			if (k.type == KessanConfig.FUSAI){
				idxs.get(1).add(i);
			}
			if (k.type == KessanConfig.JUNSHISAN){
				idxs.get(2).add(i);
			}
		}
		int max = idxs.get(0).size();
		if (idxs.get(1).size()+idxs.get(2).size() > max) max = idxs.get(1).size()+idxs.get(2).size();
		for(int i = 0; i < max; i++){
			if (i < idxs.get(0).size()){
				KamokuData k0 = zenkilist.get(((Integer)idxs.get(0).get(i)).intValue());
				buf.append(k0.kamoku);
				buf.append('\t');
				int v = k0.kariTotalSime() - k0.kashiTotalSime();
				if (v > 0){
					buf.append(""+v);
				}
				else{
					buf.append(""+(-v));
				}
				buf.append('\t');
			}
			else buf.append("\t\t");
			if (i < idxs.get(1).size()+idxs.get(2).size()){
				KamokuData k0 = zenkilist.get((i < idxs.get(1).size())?((Integer)idxs.get(1).get(i)).intValue():((Integer)idxs.get(2).get(i-idxs.get(1).size())).intValue());
				buf.append(k0.kamoku);
				buf.append('\t');
				int v = k0.kashiTotalSime() - k0.kariTotalSime();
				if (v > 0){
					buf.append(""+v);
				}
				else{
					buf.append(""+(-v));
				}
				buf.append('\n');
			}
			else buf.append("\t\n");
		}
		if (toukiJunrieki < 0){
			buf.append("当期純損失\t"+(-toukiJunrieki)+"\t\t\n");
		}
		else{
			buf.append("\t\t当期純利益\t"+toukiJunrieki+"\n");
		}
		buf.append("合計\t"+taishakuKariTotal+"\t合計\t"+taishakuKashiTotal+"\n");
		taishakuData = new String(buf);
	}

	public void saveTaishakuData(File file){
		System.out.println(file);
		this.buildTaishakuData();
		try{
			FileOutputStream out = new FileOutputStream(file);
			byte[] src = this.taishakuData.getBytes("UTF-8");
		System.out.println(src.length);
			out.write(src);
			out.close();
		}
		catch(Exception e){
			System.out.println(e);
		}
	}

	public void load(String file){
		dataClear();

		header = null;
		try{
			TabReader reader = new TabReader();
			reader.open(file);
			ArrayList<String> linebase = null;
			KessanLine owner = null;
			while((linebase = reader.read()) != null){
				System.out.println(linebase);
				if (linebase.get(0).length() > 0){
					owner = null;
				}
				KessanLine ln = new KessanLine(linebase, owner);
				if (owner == null){
					owner = ln;
				}
				if (this.header == null) this.header = ln;
				else lines.add(ln);
				
			}
			loadpath = file;
		}
		catch(Exception e){
		}
	}

	public void loadZenki(String file){
		zenki.clear();
		zenkilist.clear();

		boolean isheader = false;
		try{
			TabReader reader = new TabReader();
			reader.open(file);
			ArrayList<String> linebase = null;
			KessanLine owner = null;
			while((linebase = reader.read()) != null){
				System.out.println(linebase);
				if (!isheader) isheader = true;
				else{
					for(int i = 0; i < 2; i++){
						if (linebase.get(i*2).length() > 0){
							zenki.put(linebase.get(i*2), new KamokuData(linebase.get(i*2), Integer.parseInt(linebase.get(i*2+1))));
						}
					}
				}
			}
		}
		catch(Exception e){
		}
	}

	public int kamokuCount(){
		return zenkilist.size();
	}
	public KamokuData getKamokuData(int i){
		return zenkilist.get(i);
	}

	public int count(){
		return lines.size();
	}
	public IKessanLine get(int i){
		return lines.get(i);
	}

	public int shiwakeKashiTotal(){
		return shiwakeKashiTotalVal;
	}
	public int shiwakeKariTotal(){
		return shiwakeKariTotalVal;
	}
	public boolean isShiwakeError(){
		return shiwakeKashiTotalVal != shiwakeKariTotalVal;
	}

	class ListComp implements Comparator<KamokuData>{
		public int compare(KamokuData a, KamokuData b){
			return a.type - b.type;
		}

	}
	
	class KamokuData{
		String kamoku;
		int kurikoshi = 0;
		int kashiTotalVal = 0;
		int kariTotalVal = 0;
		int kashiTotalValSime = 0;
		int kariTotalValSime = 0;
		ArrayList<IKessanLine> list = new ArrayList<IKessanLine>();
		KurikoshiLine kurikoshiLine;
		int type = KessanConfig.ERROR;

		public KamokuData(String kamoku, int kurikoshi){
			this.kamoku = kamoku;
			this.kurikoshi = kurikoshi;
			if (kurikoshi != 0){
				kurikoshiLine = new KurikoshiLine("1/1", kamoku, kurikoshi);
			}
			type = KessanConfig.getKamoku(kamoku);
		}

		public String toString(){
			return this.kamoku;
		}
		public void clear(){
			list.clear();
			kashiTotalVal = 0;
			kariTotalVal = 0;
		}
		public void fix(){
			for(int i = 0; i < count(); i++){
				IKessanLine l = get(i);
				if (l.getKashiKamoku().equals(kamoku)){
					if (l.getPhase() == IKessanLine.ZANDAKA){
						kashiTotalVal += l.getKashiKingaku();
					}
					kashiTotalValSime += l.getKashiKingaku();
				}
				if (l.getKariKamoku().equals(kamoku)){
					if (l.getPhase() == IKessanLine.ZANDAKA){
						kariTotalVal += l.getKariKingaku();
					}
					kariTotalValSime += l.getKariKingaku();
				}
			}
		}
		public void add(IKessanLine line){
			list.add(line);
		}
		public int count(){
			return list.size()+((kurikoshiLine != null)?1:0);
		}
		public IKessanLine get(int i){
			if (kurikoshiLine != null){
				if (i == 0) return kurikoshiLine;
				i--;
			}
			return list.get(i);
		}
		public int kashiTotal(){
			return kashiTotalVal;
		}
		public int kariTotal(){
			return kariTotalVal;
		}
		public int kashiTotalSime(){
			return kashiTotalValSime;
		}
		public int kariTotalSime(){
			return kariTotalValSime;
		}
	}

	class TabReader{
		FileInputStream st;
		ByteArrayOutputStream bst;
		ArrayList<String> linebase;
		boolean isend;

		public TabReader(){
			bst = new ByteArrayOutputStream();
			linebase = new ArrayList<String>();
		}


		public void open(String file) throws IOException{
			File f = new File(file);
			isend = false;	
			st = null;
			if (!f.canRead()){
				throw new IOException("File Error");
			}
			st = new FileInputStream(f);
			bst.reset();
		}

		public ArrayList<String> read() throws IOException{
			if (isend){
				return null;
			}
			linebase.clear();
			while(true){
				int c = st.read();
				if (c == '\t' || c == '\r' || c == '\n' || c == -1){
					byte[] b = bst.toByteArray();
					bst.reset();
					if (c == -1 && b.length == 0 && linebase.size() == 0){
						close();
						break;
					}
					String v = new String(b);
				System.out.println(v);
					linebase.add(v);
					if (c == '\r' || c == '\n' || c == -1){
						if (c == '\r'){
							int c2 = st.read();
							if (c2 != '\n'){
								st.skip(-1);
							}
						}
						if (c == -1) close();
						return linebase;
					}
					continue;
				}
				bst.write(c);
			}
			return null;

		}

		public void close() throws IOException{
			if (!isend){
				isend = true;
				st.close();
			}
		}

	}


	class KurikoshiLine implements IKessanLine{
		int type = KessanConfig.ERROR;
		String hiduke;
		String kamoku;
		int kingaku;
		boolean forSime = false;
		public KurikoshiLine(String hiduke, String kamoku, int kingaku){
			this(hiduke, kamoku, kingaku, false);
		}
		public KurikoshiLine(String hiduke, String kamoku, int kingaku, boolean forSime){
			this.hiduke = hiduke;
			this.kamoku = kamoku;
			this.kingaku = kingaku;
			this.forSime = forSime;
			this.type = KessanConfig.getKamoku(kamoku);
		}
		public int getPhase(){
			return forSime?SIME:ZANDAKA;
		}
		private boolean isKari(){
			return this.type == KessanConfig.SHISAN || this.type == KessanConfig.HIYOU;
		}
		public String getHiduke(){
			return this.hiduke;
		}
		public String getTorihikisaki(){
			return "前期繰越";
		}
		public String getKariKamoku(){
			if (isKari()){
				return kamoku;
			}
			return "";
		}
		public int getKariKingaku(){
			if (isKari()){
				return kingaku;
			}
			return 0;
		}
		public String getKashiKamoku(){
			if (!isKari()){
				return kamoku;
			}
			return "";
		}
		public int getKashiKingaku(){
			if (!isKari()){
				return kingaku;
			}
			return 0;
		}
		public String getBikou(){
			return "";
		}
		
	}



	class KessanLine implements IKessanLine{
		ArrayList<String> linebase;
		KessanLine owner;
		public KessanLine(ArrayList<String> linebase, KessanLine owner){
			this.linebase = new ArrayList<String>(linebase);
			this.owner = owner;
			System.out.println(linebase);
		}
		public int getPhase(){
			return ZANDAKA;
		}
		public String getHiduke(){
			if (linebase.get(0).length() > 0) return linebase.get(0);
			if (this.owner != null) return this.owner.getHiduke();
			return "";
		}
		public String getTorihikisaki(){
			if (linebase.get(1).length() > 0) return linebase.get(1);
			if (this.owner != null) return this.owner.getTorihikisaki();
			return "";
		}
		public String getKariKamoku(){
			return linebase.get(2);
		}
		public int getKariKingaku(){
			if (linebase.get(3).length() > 0) return Integer.parseInt(linebase.get(3));
			return 0;
		}
		public String getKashiKamoku(){
			return linebase.get(4);
		}
		public int getKashiKingaku(){
			if (linebase.get(5).length() > 0) return Integer.parseInt(linebase.get(5));
			return 0;
		}
		public String getBikou(){
			return linebase.get(6);
		}

		public String toString(){
			return getHiduke()+" "+getTorihikisaki()+" 借方:"+getKariKamoku()+"("+getKariKingaku()+") 貸方:"+getKashiKamoku()+"("+getKashiKingaku()+") "+getBikou();
		}
	}


}