package fr.jzap.pdfview.util;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {

	/** Transforms (0,1,2,2,3) to (0,1,2,3) */
	public static int[] deleteDuplicatedPages(int[] pages) {
		List<Integer> result = new ArrayList<Integer>();
		int lastInt = -1;
		for (Integer currentInt : pages){
			if (lastInt!=currentInt){
				result.add(currentInt);
			}
			lastInt = currentInt;
		}
		int[] arrayResult = new int[result.size()];
		for (int i=0; i<result.size(); i++){
			arrayResult[i] = result.get(i);
		}
		return arrayResult;
	}

	/** Transforms (0, 4, 4, 6, 6, 6, 3) into (0, 1, 1, 2, 2, 2, 3) */
	public static int[] calculateIndexesInDuplicateArray(int[] originalUserPages) {
		int[] result = new int[originalUserPages.length];
		if (originalUserPages.length==0){
			return result;
		}
		
		int index = 0;
		result[0] = originalUserPages[0];
		for (int i=1; i<originalUserPages.length; i++){
			if (originalUserPages[i] != originalUserPages[i-1]){
				index++;
			}
			result[i] = index;
		}
		
		return result;
	}
	
	public static String arrayToString(int[] array){
		StringBuilder builder = new StringBuilder("[");
		for (int i = 0; i<array.length; i++){
			builder.append(array[i]);
			if (i!=array.length-1){
				builder.append(",");
			}
		}
		builder.append("]");
		return builder.toString();
	}
}
