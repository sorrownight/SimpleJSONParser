import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Luan Ta
 * @version 03/12/20
 * Stores the given Object's fields and values in a Map. Allows exportation of Object's JSON
 */
public class JSONParser
{
    private HashMap<String, Object> objMap;

    /**
     * Create a JSONParser with the given Object
     * @param obj the provided fields of this Object will be reflected onto the JSON format
     */
    public JSONParser(Object obj) throws IllegalAccessException
    {
        objMap = new HashMap<>();

        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields)
        {
            field.setAccessible(true);
            objMap.put(field.getName(), field.get(obj));
        }
    }

    /**
     * Create a JSON file from the Object given to this JSONParser
     * @param path the full path (including the file's name) of the file to be written to
     * @throws IOException
     * @throws IllegalAccessException
     */
    public void exportJSON(String path) throws IOException, IllegalAccessException
    {
        PrintWriter writer = new PrintWriter(new FileWriter(path));
        writer.print(objectToJSON().replace("\n","\\n"));
        writer.close();
    }

    private String objectToJSON() throws IllegalAccessException
    {
        StringBuilder s = new StringBuilder("{ ");
        for (Map.Entry<String,Object> entry : objMap.entrySet())
        {
            String k = entry.getKey();
            Object v = entry.getValue();
            s.append("\"").append(k).append("\": ");
            s.append(schematicAnalysis(v));
        }

        s = new StringBuilder(s.substring(0, s.length() - 1) + " }");

        return s.toString();
    }

    private String schematicAnalysis(Object v) throws IllegalAccessException
    {
        // System.out.println(v.getClass());
        StringBuilder s = new StringBuilder();
        if (v == null)
            s.append("null,");
        else if (  v.getClass().equals(Integer.class)
                || v.getClass().equals(Double.class)
                || v.getClass().equals(Float.class)
                || v.getClass().equals(Boolean.class)
                || v.getClass().equals(Long.class)
                || v.getClass().equals(Short.class)
                || v.getClass().equals(Byte.class)) // Primitive format: "Field_Name": Value
            s.append(v).append(",");

        else if (  v.getClass().equals(String.class)
                || v.getClass().equals(Character.class)) // String format: "Field_Name": "Value"
            s.append("\"").append((v)).append("\",");

        else if (v.getClass().isArray()) // Array format: [Object, Object, Object]
        {
            s.append("[");
            int length = Array.getLength(v);
            for (int i = 0; i < length; i++)
                s.append(schematicAnalysis(Array.get(v, i)));
            s = new StringBuilder(s.substring(0, s.length() - 1) + "],");
        }

        else // Object format: "Field_Name": {...}
        {
            JSONParser tmp = new JSONParser(v);
            s.append(tmp.objectToJSON()).append(",");
        }

        return s.toString();
    }
}

