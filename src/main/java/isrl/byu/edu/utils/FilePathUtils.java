package isrl.byu.edu.utils;

public class FilePathUtils {

    public static String getParentFullPath(String fullPath)
    {
        //root
        if(fullPath == "")
        {
            return null;
        }

        return fullPath.substring(0, fullPath.lastIndexOf("/"));
    }
    public static String getFileName(String fullPath)
    {
        //root
        if(fullPath == "")
        {
            return null;
        }

        while (fullPath.substring(fullPath.length() - 1).equals("/")) {
            fullPath = fullPath.substring(0, fullPath.length() - 1);
        }
        if (fullPath.isEmpty()) {
            return "";
        }
        return fullPath.substring(fullPath.lastIndexOf("/") + 1);
    }
    public static String getFullPath(String parentFullPath, String name)
    {
        if (parentFullPath == null) {
            return name;
        } else {
            return parentFullPath + "/" + name;
        }
    }
}
