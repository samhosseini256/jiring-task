package com.example.service;

import com.example.model.FrequencyModel;
import com.example.model.NewsModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class NewsAnalyzer {

    @Value("${words.goodWords}")
    private List<String> goodWords;

    @Value("${words.badWords}")
    private List<String> badWords;

    private boolean isGoodNews(String title) {

        String[] sentenceWords = title.split(" ");

        List<String> sentenceGoodWords = Arrays

                .stream(sentenceWords)
                .filter(goodWords::contains)

                .toList();

        return ((double) sentenceGoodWords.size() / sentenceWords.length) > 0.5;
    }

    private boolean isBadNews(String title) {

        String[] sentenceWords = title.split(" ");

        List<String> sentenceBadWords = Arrays

                .stream(sentenceWords)
                .filter(badWords::contains)

                .toList();

        return ((double) sentenceBadWords.size() / sentenceWords.length) > 0.5;
    }

    private boolean isUniqueNews(NewsModel news){
        return isGoodNews(news.getTitle()) && news.getPriority() > 6;
    }

    public NewsModel filterNewsByFrequency(NewsModel news, FrequencyModel frq) {

        if (frq == null || !frq.isEnable()) return news;
        validationFrequency(frq);

        if ((frq.getPriorityTarget()!=null)
                && Math.abs(news.getPriority() - frq.getPriorityTarget()) > frq.getPriorityDistance()) {
            System.err.println("Filtered: Range");
            return null;
        }

        if (frq.getSendJustGoodNews() != null
                && isGoodNews(news.getTitle()) != frq.getSendJustGoodNews()) {
            System.err.println("Filtered: Good");
            return null;
        }

        if (frq.getSendJustBadNews() != null
                && isBadNews(news.getTitle()) != frq.getSendJustBadNews()) {
            System.err.println("Filtered: Bad");
            return null;
        }

        news.setGoodNews(isGoodNews(news.getTitle()));
        news.setUniqueNews(isUniqueNews(news));
        return news;
    }

    //todo to bean validation
    private void validationFrequency(FrequencyModel frq){
        if (
                frq.getSendJustGoodNews() != null
                        && frq.getSendJustBadNews() != null
                        && frq.getSendJustGoodNews().equals(frq.getSendJustBadNews())) {
            throw new RuntimeException("Exception: LOGICAL CONFLICT IN GOO OR BAD NEWS!");
        }
        if ((
                frq.getPriorityTarget() != null && frq.getPriorityDistance() ==null)
                ||
                frq.getPriorityTarget() == null && frq.getPriorityDistance() !=null)
            throw new RuntimeException("Exception: EITHER SET BOTH OR BOTH ARE NULL");
    }

}