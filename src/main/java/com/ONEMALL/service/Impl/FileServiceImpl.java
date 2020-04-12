package com.ONEMALL.service.Impl;

import com.ONEMALL.service.IFileService;
import com.ONEMALL.util.FTPUtil;
import com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;

@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    /**
     *
     * @param file
     * @param path
     * @return
     */
    public String upload(MultipartFile file, String path){
        String filename = file.getOriginalFilename();
        //extension
        String fileExtensionName = filename.substring(filename.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("Start uploading file, name:{},path:{},newFileName:{}",filename,path, uploadFileName);

        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path, uploadFileName);
        try {
            file.transferTo(targetFile);
            //File upload success at this step
            //Upload to FTP server
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //Delete upload file
            targetFile.delete();
        }catch (IOException e){
            logger.error("Error uploading file",e);
            return null;
        }
        return targetFile.getName();
    }
}
