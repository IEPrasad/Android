import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JobScraper {

    public static class Job {
        String title;
        String company;
        String location;
        String link;

        public Job(String title, String company, String location, String link) {
            this.title = title;
            this.company = company;
            this.location = location;
            this.link = link;
        }

        @Override
        public String toString() {
            return title + " | " + company + " | " + location + " | " + link;
        }
    }

    public static List<Job> scrapeIndeed(String keyword, String location) {
        List<Job> jobList = new ArrayList<>();
        try {
            String url = "https://www.indeed.com/jobs?q=" + keyword + "&l=" + location;
            Document doc = Jsoup.connect(url).userAgent("Mozilla").timeout(5000).get();

            Elements jobs = doc.select("div.job_seen_beacon");
            for (Element job : jobs) {
                String title = job.select("h2.jobTitle").text();
                String company = job.select("span.companyName").text();
                String loc = job.select("div.companyLocation").text();
                String link = "https://www.indeed.com" + job.select("a").attr("href");

                jobList.add(new Job(title, company, loc, link));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jobList;
    }

    public static void main(String[] args) {
        List<Job> jobs = scrapeIndeed("software+developer", "remote");
        for (Job job : jobs) {
            System.out.println(job);
        }
    }
}
