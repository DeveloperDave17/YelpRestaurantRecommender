package com.yelp.restaurantFinder;


import GraphCreator.Graph;
import GraphCreator.GraphFactory;
import Loader.ExtendibleHashTable;
import Loader.ExtendibleHashTableFactory;
import Loader.LoaderApplication;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * The controller for our application, notably only the root of the web application is used since our front-end
 * doesn't require more paths.
 *
 * @author  David Hennigan and Anthony Impellizzeri
 */
@Controller
public class RestaurantFinderController {

    List<Review> reviews;

    ExtendibleHashTable eht;

    Set<String> uniqueWords;

    Graph businessGraph;

    @PostConstruct
    public void init() throws IOException {
        HashMap<String,Business> businessHashMap = GsonDataRetriever.getBusinessHashMap();
        reviews = GsonDataRetriever.getReviewList(businessHashMap);
        eht = ExtendibleHashTableFactory.createExtendibleHashTable();
        uniqueWords = LoaderApplication.poolUniqueWords(reviews);
        businessGraph = GraphFactory.loadGraph();
    }


    /**
     * This function will be called whenever a user first visits the website, greets them with a search field.
     * @return an index page for the user, generated using thymeleaf in conjunction with springboot.
     */
    @GetMapping( value = "/")
    public ModelAndView index() {
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("query", new Query());
        return mav;
    }

    /**
     * Handles the users post requests regarding restaurant name and returns the result of the search, a custom html
     * page is generated if the restaurant does not exist in our database.
     * @param query the name of the restaurant the user provides.
     * @return ModelAndView which is a generated html page for the user, includes the results of their search.
     * @throws IOException if the data file is not found on the server.
     */
    @PostMapping( value = "/")
    public ModelAndView result(@ModelAttribute Query query) throws IOException {
        List<String> businesses = Similarity.calculation(query.getName(), reviews);
        String traversalResult = businessGraph.shortestPath(query.getName());
        if (businesses.size() == 2) {
            // Do if results were found with a traversal
            ModelAndView mav = new ModelAndView("resultWithTraversal");
            // sets the user up for another possible query
            mav.addObject("query", new Query());
            mav.addObject("prevQuery", query);
            mav.addObject("businesses", businesses);
            ClusterResult clusterResult = new ClusterResult(ClusterFinder.find(query.getName(), uniqueWords, eht));
            mav.addObject("clusterResult", clusterResult);
            int disjointCount = businessGraph.numDisjointSets();
            mav.addObject("disjointSets", new DisjointSets(disjointCount));
            mav.addObject("graphData", new GraphData(traversalResult));
            return mav;
        } else{
            ModelAndView mav = new ModelAndView("noResult");
            mav.addObject("query", new Query());
            return mav;
        }
    }
}
