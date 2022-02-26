package jp.yupj.kessantool;

import java.awt.*;
import java.io.*;

public class KessanConfig {

	public static final int SHISAN = 1;
	public static final int FUSAI = 2;
	public static final int JUNSHISAN = 3;
	public static final int SHUEKI = 4;
	public static final int HIYOU = 5;
	public static final int SONOTA = 9;
	public static final int ERROR = -1;

	public static final String[][] kamokulist = {
		{
			"売掛金",
			"預金",
			"普通預金",
			"仮払税金",
			"前払費用",
			"現金",
			"事業主貸"
		},
		{
			"クレジット未払金",
			"事業主借"
		},
		{
			"元入金"
		},
		{
			"売上"
		},
		{
			"費用"
		},
		{
		}
	};


	public static Color[] colors = {
		new Color(0xff0000), new Color(0x0000ff), new Color(0xff9000), new Color(0x00c000), new Color(0x802080), new Color(0x333333), new Color(0xa0a0a0)
	};
	public static Color getColor(int kamoku){
		if (kamoku >= 1 && kamoku <= 5){
			return colors[kamoku-1];
		}
		if (kamoku == SONOTA) return colors[5];
		return colors[6];
	}
	public static int getKamoku(String text){
		int idx = -1;
		for(int i = 0; i < kamokulist.length; i++){
			for(int j = 0; j < kamokulist[i].length; j++){
				if (kamokulist[i][j].equals(text)){
					idx = i;
					break;
				}
			}
		}
		if (idx >= 0 && idx < 5) return idx+1;
		if (idx == 5) return SONOTA;
		return ERROR;
	}

	public static Font[] fonts = new Font[2];

	static{
		for(int i = 0; i < 2; i++){
			Font f = null;
			try{
				if (i == 0){
					f = Font.createFont(Font.TRUETYPE_FONT, new File("/System/Library/Fonts/ヒラギノ角ゴシック W3.ttc"));
				}
				else if (i == 1){
					f = Font.createFont(Font.TRUETYPE_FONT, new File("/System/Library/Fonts/ヒラギノ角ゴシック W6.ttc"));
				}
			}
			catch(Exception e){
				f = Font.getFont(Font.SANS_SERIF);
			}
			fonts[i] = f;
		}
	}


	public static Font getFont(float sz){
		return fonts[0].deriveFont(sz);
	}
	public static Font getFontBold(float sz){
		return fonts[1].deriveFont(sz);
	}
}