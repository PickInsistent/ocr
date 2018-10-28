package com.roey.ocr.pageAnalysis;

import com.roey.ocr.entity.Cell;
import com.roey.ocr.entity.FontRange;
import com.roey.ocr.preprocess.Division;
import com.roey.ocr.util.ImageHandleUtil;
import com.roey.ocr.util.ImageShowUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description
 *
 * @author: lizhanping
 * @date: 2018/10/24 16:17
 **/
public class Analysis {

    public static Map<String, List<int[][]>> simpleMap = new HashMap<>();

    public static List<List<String>> analysisTable(BufferedImage image) {
        List<List<String>> result = new ArrayList<>();
        List<Cell> cells = Division.divideCell(image);
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < cells.size(); i++) {
            if (i != 0 && cells.get(i).getRowNum() != cells.get(i - 1).getRowNum()) {
                result.add(rows);
                rows = new ArrayList<>();
            }
            StringBuilder values = new StringBuilder();
            for (FontRange fontRange : cells.get(i).getValues()) {
                String value = analysisFont(image, fontRange);
                values.append(value);
            }
            rows.add(values.toString());
        }
        return result;
    }

    public static String analysisFont(BufferedImage image, FontRange fontRange) {
        int x1 = fontRange.getX1();
        int y1 = fontRange.getY1();
        int x2 = fontRange.getX2();
        int y2 = fontRange.getY2();
        BufferedImage fontImage = image.getSubimage(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
//        ImageShowUtil.img(fontImage);
        int[][] unknownFontData = getFontData(fontImage);
        int score = 0;
        String result = "";
        int lastScore = Integer.MAX_VALUE;
        for (Map.Entry<String, List<int[][]>> entry : simpleMap.entrySet()) {
//            System.out.println(">>>>>>>>>" + entry.getKey() + ">>>>>>>>>>>");
            score = contrastPixel(entry.getValue().get(0), unknownFontData);
//            System.out.println("<<<<<<<<<" + score + "<<<<<<<<<<<");
            if (lastScore > score) {
//                if(score>80){
//                    continue;
//                }
                result = entry.getKey();
                lastScore = score;
            }
        }
        System.out.println("<<<<<<<<<" + score + "<<<<<<<<<<<");
        System.out.println("<<<<<<<<<" + result + "<<<<<<<<<<<");
        return result;
    }

    public static int contrastPixel(int[][] simpleData, int[][] unknowData) {
//        System.out.println(">>>>>>>>>simpleData>>>>>>>>>>>");
//        for (int i = 0; i < simpleData.length; i++) {
//            System.out.println(Arrays.toString(simpleData[i]));
//        }
//        System.out.println("<<<<<<<<<unknowData<<<<<<<<<<<");
//        for (int i = 0; i < unknowData.length; i++) {
//            System.out.println(Arrays.toString(unknowData[i]));
//        }
        if (Math.abs(simpleData.length - unknowData.length + simpleData[0].length - unknowData[0].length) > 5) {
            return Integer.MAX_VALUE;
        }
        int result = 0;
        int row;
        int column;
        if (simpleData.length > unknowData.length) {
            row = unknowData.length;
        } else {
            row = simpleData.length;
        }
        if (simpleData[0].length > unknowData[0].length) {
            column = unknowData[0].length;
        } else {
            column = simpleData[0].length;
        }
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                if (simpleData[i][j] != unknowData[i][j]) {
                    result++;
                }
            }
        }
        if (simpleData.length > row) {
            for (int i = row; i < simpleData.length; i++) {
                for (int j = 0; j < simpleData[i].length; j++) {
                    if (simpleData[i][j] == 1) {
                        result++;
                    }
                }
            }
        }
        if (simpleData[0].length > column) {
            for (int i = column; i < simpleData[0].length; i++) {
                for (int j = 0; j < simpleData.length; j++) {
                    if (simpleData[j][i] == 1) {
                        result++;
                    }
                }
            }
        }
        if (unknowData.length > row) {
            for (int i = row; i < unknowData.length; i++) {
                for (int j = 0; j < unknowData[i].length; j++) {
                    if (unknowData[i][j] == 1) {
                        result++;
                    }
                }
            }
        }
        if (unknowData[0].length > column) {
            for (int i = column; i < unknowData[0].length; i++) {
                for (int j = 0; j < unknowData.length; j++) {
                    if (unknowData[j][i] == 1) {
                        result++;
                    }
                }
            }
        }
        int diff = 0;
        if (simpleData.length > row && simpleData[0].length > column) {
            diff = (simpleData.length - row) * (simpleData[0].length - column);
        }
        if (unknowData.length > row && unknowData[0].length > column) {
            diff = (unknowData.length - row) * (unknowData[0].length - column);
        }
        return result - diff;
    }

    public static void loadSimpleData() throws IOException {
        //汇缴、年终结息、公积金提取还款
        String basePath = Analysis.class.getClassLoader().getResource(".").getFile() + "fontsimple/";
        simpleMap.put("序", getSimpleImage(basePath + "xu"));
        simpleMap.put("号", getSimpleImage(basePath + "hao"));
        simpleMap.put("记", getSimpleImage(basePath + "ji2"));
        simpleMap.put("账", getSimpleImage(basePath + "zhang"));
        simpleMap.put("日", getSimpleImage(basePath + "ri"));
        simpleMap.put("期", getSimpleImage(basePath + "qi"));
        simpleMap.put("归", getSimpleImage(basePath + "gui"));
        simpleMap.put("集", getSimpleImage(basePath + "ji3"));
        simpleMap.put("和", getSimpleImage(basePath + "he"));
        simpleMap.put("业", getSimpleImage(basePath + "ye"));
        simpleMap.put("务", getSimpleImage(basePath + "wu"));
        simpleMap.put("类", getSimpleImage(basePath + "lei"));
        simpleMap.put("型", getSimpleImage(basePath + "xing"));
        simpleMap.put("摘", getSimpleImage(basePath + "zhai"));
        simpleMap.put("要", getSimpleImage(basePath + "yao"));
        simpleMap.put("发", getSimpleImage(basePath + "fa"));
        simpleMap.put("生", getSimpleImage(basePath + "sheng"));
        simpleMap.put("额", getSimpleImage(basePath + "e"));
        simpleMap.put("利", getSimpleImage(basePath + "li"));
        simpleMap.put("原", getSimpleImage(basePath + "yuan"));
        simpleMap.put("因", getSimpleImage(basePath + "yin"));
        simpleMap.put("方", getSimpleImage(basePath + "fang"));
        simpleMap.put("式", getSimpleImage(basePath + "shi"));
        simpleMap.put("余", getSimpleImage(basePath + "yu"));


        simpleMap.put(".", getSimpleImage(basePath + "dian"));
        simpleMap.put(",", getSimpleImage(basePath + "douhao"));
        simpleMap.put("-", getSimpleImage(basePath + "hengxian"));
        simpleMap.put("0", getSimpleImage(basePath + "0"));
        simpleMap.put("1", getSimpleImage(basePath + "1"));
        simpleMap.put("2", getSimpleImage(basePath + "2"));
        simpleMap.put("3", getSimpleImage(basePath + "3"));
        simpleMap.put("4", getSimpleImage(basePath + "4"));
        simpleMap.put("5", getSimpleImage(basePath + "5"));
        simpleMap.put("6", getSimpleImage(basePath + "6"));
        simpleMap.put("7", getSimpleImage(basePath + "7"));
        simpleMap.put("8", getSimpleImage(basePath + "8"));
        simpleMap.put("9", getSimpleImage(basePath + "9"));
        simpleMap.put("公", getSimpleImage(basePath + "gong"));
        simpleMap.put("号", getSimpleImage(basePath + "hao"));
        simpleMap.put("还", getSimpleImage(basePath + "huan"));
        simpleMap.put("汇", getSimpleImage(basePath + "hui"));
        simpleMap.put("积", getSimpleImage(basePath + "ji"));
        simpleMap.put("缴", getSimpleImage(basePath + "jiao"));
        simpleMap.put("结", getSimpleImage(basePath + "jie"));
        simpleMap.put("金", getSimpleImage(basePath + "jin"));
        simpleMap.put("款", getSimpleImage(basePath + "kuan"));
        simpleMap.put("年", getSimpleImage(basePath + "nian"));
        simpleMap.put("度", getSimpleImage(basePath + "du"));
        simpleMap.put("取", getSimpleImage(basePath + "qu"));
        simpleMap.put("提", getSimpleImage(basePath + "ti"));
        simpleMap.put("息", getSimpleImage(basePath + "xi"));
        simpleMap.put("终", getSimpleImage(basePath + "zhong"));
        simpleMap.put("部", getSimpleImage(basePath + "bu"));
        simpleMap.put("分", getSimpleImage(basePath + "fen"));

        simpleMap.put("偿", getSimpleImage(basePath + "chang"));
        simpleMap.put("购", getSimpleImage(basePath + "gou"));
        simpleMap.put("房", getSimpleImage(basePath + "fang2"));
        simpleMap.put("贷", getSimpleImage(basePath + "dai"));
        simpleMap.put("本", getSimpleImage(basePath + "ben"));
        simpleMap.put("其", getSimpleImage(basePath + "qi2"));
        simpleMap.put("它", getSimpleImage(basePath + "ta"));


        simpleMap.put("共", getSimpleImage(basePath + "gong2"));
        simpleMap.put("条", getSimpleImage(basePath + "tiao"));
        simpleMap.put("录", getSimpleImage(basePath + "lu"));
        simpleMap.put("第", getSimpleImage(basePath + "di"));
        simpleMap.put("/", getSimpleImage(basePath + "xiegang"));
        simpleMap.put("下", getSimpleImage(basePath + "xia"));
        simpleMap.put("一", getSimpleImage(basePath + "yi"));
        simpleMap.put("页", getSimpleImage(basePath + "ye2"));

    }

    public static List<int[][]> getSimpleImage(String path) throws IOException {
        List<int[][]> result = new ArrayList<>();
        File rootFile = new File(path);
        File[] files = rootFile.listFiles();
        try {
            for (int i = 0; i < files.length; i++) {
//                boolean isAvl = files[i].getAbsolutePath().contains("png")||files[i].getAbsolutePath().contains("jpg");
//                if (isAvl) {
                BufferedImage image = ImageIO.read(files[i]);
                result.add(getFontData(image));
//                }
            }
        } catch (Exception e) {
            System.out.println(path);
        }
        return result;
    }

    public static int[][] getFontData(BufferedImage image) {
        int[] hp = ImageHandleUtil.imageProjection(image, ImageHandleUtil.HORIZONTAL);
        int[] vp = ImageHandleUtil.imageProjection(image, ImageHandleUtil.VERTICAL);
        int x1 = edgeDetection(vp, true);
        int x2 = edgeDetection(vp, false);
        int y1 = edgeDetection(hp, true);
        int y2 = edgeDetection(hp, false);
        image = image.getSubimage(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
        int w = image.getWidth();
        int h = image.getHeight();
        int[][] data = new int[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (image.getRGB(j, i) != Color.BLACK.getRGB()) {
                    data[i][j] = 0;
                } else {
                    data[i][j] = 1;
                }
            }

        }
        return data;
    }

    public static int edgeDetection(int[] data, boolean left) {
        int result = 0;
        if (left) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] != 0) {
                    result = i;
                    break;
                }
            }
        } else {
            for (int i = data.length - 1; i >= 0; i--) {
                if (data[i] != 0) {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        loadSimpleData();
        BufferedImage image = ImageIO.read(new File("C:\\Users\\B-0036\\Desktop\\ocr\\huangshi\\huangshi_2.png"));
//        BufferedImage image = ImageIO.read(new File("E:\\chifeng_1.png"));
        image = ImageHandleUtil.binaryImage(image, 180);
        ImageShowUtil.img(image);
        List<List<String>> lists = analysisTable(image);
        for (int i = 0; i < lists.size(); i++) {
            System.out.println(lists.get(i));
        }
    }
}