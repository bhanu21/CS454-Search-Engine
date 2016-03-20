package HW4;

import java.util.Comparator;
import java.util.TreeMap;

public class SearchDetail implements Comparable<SearchDetail>{
	public String url = null;
	public String title = null;
	public Double cosSimilarity = 0.0;
	public Integer searchedWordsFound=0;
	public Double customRanking = 0.0;
	public Double pageRanking = 0.0;
	public Double tfidf = 0.0;
	
	public SearchDetail(String url,String title,Double cosSimilarity,int searchedWordsFound,Double customRanking){
		this.url = url;
		this.title = title;
		this.cosSimilarity = cosSimilarity;
		this.searchedWordsFound=searchedWordsFound;
		this.customRanking = customRanking;
		
	}
	
	public SearchDetail(){
		
	}
	public int compareTo(SearchDetail e2) {

		int res = e2.searchedWordsFound.compareTo(this.searchedWordsFound);
        if(res==0)
        	res = e2.customRanking.compareTo(this.customRanking);
        return res != 0 ? res : 1;
	}
	
    static Comparator<SearchDetail> getCosSimilarityComparator() {
        return new Comparator<SearchDetail>() {
            @Override
            public int compare(SearchDetail e1, SearchDetail e2) {
            	int res = e2.cosSimilarity.compareTo(e1.cosSimilarity);
                if(res==0)
                	res = e2.searchedWordsFound.compareTo(e1.searchedWordsFound);
                if(res==0)
                	res = e2.customRanking.compareTo(e1.customRanking);
                return res != 0 ? res : 1;
            }
        };
    }

}
