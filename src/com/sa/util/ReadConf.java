package com.sa.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.sa.base.ConfManager;

public class ReadConf {

	/**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
	public void readFileByLines() {
		String fileName = System.getProperty("user.dir")+"/bin/conf/setting.conf";
//		String fileName = System.getProperty("user.dir")+"/conf/setting.conf";

		this.readFileByLines(fileName);
	}

	/**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public void readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));

            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
            	if (!tempString.startsWith("#")) {
            		String[] arr = tempString.split(":=");
            		if (2 == arr.length && !"".equals(arr[0].trim()) && !"".equals(arr[1].trim())) {
            			ConfManager.CONF_MAP.put(arr[0].trim(), arr[1].trim());
            		}
            	}
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
    }

}
