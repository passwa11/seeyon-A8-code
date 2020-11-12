package com.monkeyk.sos.web.controller;

import com.monkeyk.sos.domain.oauth.OauthClientDetails;
import com.monkeyk.sos.service.dto.OauthClientDetailsDto;
import com.monkeyk.sos.service.OauthService;
import com.monkeyk.sos.web.oauth.OauthClientDetailsDtoValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handle 'client_details' management
 *
 * @author Shengzhao Li
 */
@Controller
public class ClientDetailsController {


    @Autowired
    private OauthService oauthService;

    @Autowired
    private OauthClientDetailsDtoValidator clientDetailsDtoValidator;

    @RequestMapping("deleteClient")
    public String deleteClient(@RequestParam("clientId") String clientId) {
        oauthService.deleteClient(clientId);
        return "index";
    }

    @RequestMapping("client_details")
    public String clientDetails(Model model) {
        List<OauthClientDetailsDto> clientDetailsDtoList = oauthService.loadAllOauthClientDetailsDtos();
        model.addAttribute("clientDetailsDtoList", clientDetailsDtoList);
//        return "clientdetails/client_details";
        return "index";
    }


    /*
     * Logic delete
     * */
    @RequestMapping("archive_client/{clientId}")
    public String archiveClient(@PathVariable("clientId") String clientId) {
        oauthService.archiveOauthClientDetails(clientId);
        return "redirect:../client_details";
    }

    /*
     * Test client
     * */
    @RequestMapping("test_client/{clientId}")
    public String testClient(@PathVariable("clientId") String clientId, Model model) {
        OauthClientDetailsDto clientDetailsDto = oauthService.loadOauthClientDetailsDto(clientId);
        model.addAttribute("clientDetailsDto", clientDetailsDto);
        return "clientdetails/test_client";
    }


    /*
     * Register client
     * */
    @RequestMapping(value = "register_client", method = RequestMethod.GET)
    public String registerClient(Model model) {
        model.addAttribute("formDto", new OauthClientDetailsDto());
        return "clientdetails/register_client";
    }

    @RequestMapping(value = "register_client/{clientId}", method = RequestMethod.GET)
    public String registerClient(@PathVariable("clientId") String clientId, Model model) {
        OauthClientDetails details = oauthService.loadOauthClientDetails(clientId);
        model.addAttribute("formDto", details);
        return "clientdetails/edit_client";
    }

    @RequestMapping(value = "register_client/{clientId}", method = RequestMethod.POST)
    public String editRegisterClient(@PathVariable("clientId") String clientId, @ModelAttribute("formDto") OauthClientDetailsDto formDto, BindingResult result) {
        if (result.hasErrors()) {
            return "clientdetails/edit_client";
        }
        oauthService.updateClientDetails(formDto);
        return "redirect:/./";
    }

    /*
     * Submit register client
     * */
    @RequestMapping(value = "register_client", method = RequestMethod.POST)
    public String submitRegisterClient(@ModelAttribute("formDto") OauthClientDetailsDto formDto, BindingResult result) {
//        System.out.print("submitRegisterClient==" + formDto.getClientId() + "====" + formDto.getClientSecret());
        clientDetailsDtoValidator.validate(formDto, result);
        if (result.hasErrors()) {
            return "clientdetails/register_client";
        }
        oauthService.registerClientDetails(formDto);
        return "redirect:";
    }


}