package com.yelp.restaurantFinder;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/**
 * The controller for our application, notably only the root of the web application is used since our front-end
 * doesn't require more paths.
 *
 * @author  David Hennigan and Anthony Impellizzeri
 */
@Controller
public class RestaurantFinderController {

    List<Review> reviews;

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
        if ( reviews == null ){
            HashMap<String,Business> businessHashMap = GsonDataRetriever.getBusinessHashMap();
            reviews = GsonDataRetriever.getReviewList(businessHashMap);
        }
        List<String> businesses = Similarity.calculation(query.getName(), reviews);
        if (businesses.size() == 2) {
            // Do if results were found
            ModelAndView mav = new ModelAndView("result");
            // sets the user up for another possible query
            mav.addObject("query", new Query());
            mav.addObject("prevQuery", query);
            mav.addObject("businesses", businesses);
            return mav;
        } else{
            ModelAndView mav = new ModelAndView("noResult");
            mav.addObject("query", new Query());
            return mav;
        }
    }
}
