package jp.yupj.kessantool;


interface IKessanLine{
		public static final int ZANDAKA = 1;
		public static final int SHUSEI = 2;
		public static final int SIME = 3;

		public int getPhase();
		public String getHiduke();
		public String getTorihikisaki();
		public String getKashiKamoku();
		public int getKashiKingaku();
		public String getKariKamoku();
		public int getKariKingaku();
		public String getBikou();
}