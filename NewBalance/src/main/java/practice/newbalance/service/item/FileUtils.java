package practice.newbalance.service.item;

import jakarta.servlet.ServletContext;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUtils {

    @Autowired
    private ServletContext ctx; // 내장 tomcat이기 때문에

    private static final String BASE_PATH = "c:/upload";

    public void makeFolders(String path) {
        File folder = new File(path);

        if(!folder.exists()) {
            folder.mkdirs();
        }
    }

    public String getBasePath(String... moreFolder) {
        String temp = "";
        for(String s : moreFolder) {
            temp += s;
        }
//        return ctx.getRealPath(temp); // tomcat이 내장되어있기 때문에 tomcat이 인식하는 경로를 설정해야한다.
        return BASE_PATH + String.join("/", temp); // 로컬환경에 저장하기위해 ContextServlet 경로 사용안함
    }

    // 확장자 얻어오기
    public String getExt(String fileNm){
        return fileNm.substring(fileNm.lastIndexOf(".") + 1);
    }

    //랜덤 파일명 리턴
    public String getRandomFileNm(String fileNm){
        return UUID.randomUUID().toString() + "." + getExt(fileNm);
    }

//    public String transferTo(MultipartFile mf, boolean createThumb, String... target) throws Exception {
    public String transferTo(MultipartFile mf, String... target) throws Exception {
        String fileNm = null;
        String basePath = getBasePath(target);
//        String basePath = BASE_PATH + String.join("/", target);
        makeFolders(basePath);
        File file;

        try {
            fileNm = getRandomFileNm(mf.getOriginalFilename());
            file = new File(basePath, fileNm);
            mf.transferTo(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        if(createThumb) {
//            makeThumbnail(file, basePath, 550);
//        }
        return fileNm;
    }

    public String transferToThumbnail(MultipartFile mf, String... target) throws Exception {
        String fileNm = null;
        String basePath = getBasePath(target);
        makeFolders(basePath);
        File file;

        try {
            fileNm = getRandomFileNm(mf.getOriginalFilename());
            file = new File(basePath, fileNm);
            mf.transferTo(file);

            // 썸네일 리사이즈 처리
            File thumbnailFile = new File(basePath, "thumbnail_" + fileNm);
            Thumbnails.of(file)
                    .size(200, 200)
                    .toFile(thumbnailFile); // 크기 조정 (200x200)

            return thumbnailFile.getName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
