package com.roey.ocr.sample;

import com.roey.ocr.entity.CharArea;
import com.roey.ocr.preprocess.Division;
import com.roey.ocr.util.ImageHandleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * description
 *
 * @author: lizhanping
 * @date: 2018/10/24 16:56
 **/
@Component
public class SampleGenerate {

    @Autowired
    private Division division;

    public void generateYzmSample(String imagePath, int grayBoundary, String tag) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        image = ImageHandleUtil.binaryImage(image, grayBoundary);
        String date = LocalDate.now().toString().replace("-", "");
        String dirPath = "E:\\yzm\\" + tag + "\\" + date + "\\" + date;
        generateGjjOcrSample(image, dirPath, "jpg");
    }

    public void generateShenYueSample(String imagePath, int grayBoundary, Integer... columnIndexes) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        image = ImageHandleUtil.binaryImage(image, grayBoundary);
        image = ImageHandleUtil.removeBothEnds(image);
        image = ImageHandleUtil.optimizeColumnSpace(image, columnIndexes);
        String date = LocalDate.now().toString().replace("-", "");
        String dirPath = "E:\\shenyue\\" + date + "\\";
        generateGjjOcrSample(image, dirPath, "png");
    }


    public void generateGjjOcrSample(BufferedImage image, String dirPath, String imageType) throws IOException {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        List<CharArea> charAreas = division.divideChar(image);
        for (int i = 0; i < charAreas.size(); i++) {
            BufferedImage charImage = image.getSubimage(charAreas.get(i).getX1(), charAreas.get(i).getY1(), charAreas.get(i).getWidth(), charAreas.get(i).getHeight());
            ImageIO.write(charImage, imageType, new File(dirPath + +i + "." + imageType));
        }
    }
}
