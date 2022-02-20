package jp.yupj.kessantool;


public class KessanTool{

	public static void main(String[] argv){
		if (argv.length < 2){
			System.out.println("file not set");
			return;
		}
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		String file = argv[0];
		String filezenki = argv[1];

		KessanData data = new KessanData();
		data.load(file);
		data.loadZenki(filezenki);
		data.fix();

		KessanFrame f = new KessanFrame();
		f.setData(data);
		f.open();
	}

}