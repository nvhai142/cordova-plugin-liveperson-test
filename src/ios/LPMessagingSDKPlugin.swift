//
//  LPMessagingSDKPlugin.swift
//  LPSDKCordovaSample
//
//  Created by jbeadle.
//
//

import Foundation
import LPMessagingSDK

extension String {
    
    /// Create `Data` from hexadecimal string representation
    ///
    /// This takes a hexadecimal representation and creates a `Data` object. Note, if the string has any spaces or non-hex characters (e.g. starts with '<' and with a '>'), those are ignored and only hex characters are processed.
    ///
    /// - returns: Data represented by this hexadecimal string.
    
    func hexadecimal() -> Data? {
        var data =   Data(capacity: self.count/2)
        
        let regex = try! NSRegularExpression(pattern: "[0-9a-f]{1,2}", options: .caseInsensitive)
        regex.enumerateMatches(in: self, range: NSMakeRange(0, utf16.count)) { match, flags, stop in
            let byteString = (self as NSString).substring(with: match!.range)
            var num = UInt8(byteString, radix: 16)!
            data.append(&num, count: 1)
        }
        
        guard data.count > 0 else { return nil }
        
        return data
    }
    
}

@objc(LPMessagingSDKPlugin) class LPMessagingSDKPlugin: CDVPlugin, ConversationDelegate {
    
    var conversationScreen : ConversationVC?
    
    var conversationQuery: ConversationParamProtocol?

    // adding delegates and callbacks for Cordova to notify javascript wrapper when functions complete
    var callBackCommandDelegate: CDVCommandDelegate?
    var callBackCommand:CDVInvokedUrlCommand?
  
    var registerLpPusherCallbackCommandDelegate: CDVCommandDelegate?
    var registerLpPusherCallbackCommand: CDVInvokedUrlCommand?

    
    var globalCallbackCommandDelegate: CDVCommandDelegate?
    var globalCallbackCommand: CDVInvokedUrlCommand?
    
    var lpAccountNumber:String?
    
    override init() {
        super.init()
    }
    
    override func pluginInitialize() {
        print("@@@ iOS pluginInitialize")
    }

    @objc(lp_sdk_init:)
    func lp_sdk_init(command: CDVInvokedUrlCommand) {
        guard let lpAccountNumber = command.arguments.first as? String else {
            print("Can't init without brandID")
            return
        }
        guard let appInstallID = command.arguments[2] as? String else {
            print("Can't init without AppInstallID")
            return
        }
        self.lpAccountNumber = lpAccountNumber
        
        print("lpMessagingSdkInit brandID --> \(lpAccountNumber)")
        
        let monitoringInitParams = LPMonitoringInitParams(appInstallID: appInstallID)

        do {
            try LPMessaging.instance.initialize(lpAccountNumber, monitoringInitParams: monitoringInitParams)
            
            // only set config if we have a valid argument
            // deprecated - should be done through direct editing of this function  for the relevant options
            // in which case move the setSDKConfigurations call outside of this wrapping loop and call on init every time
            
            // if let config = command.arguments.last as? [String:AnyObject] {
            //     setSDKConfigurations(config: config)
            // }
            
            let configurations = LPConfig.defaultConfiguration
            configurations.fileSharingFromAgent = true
            configurations.fileSharingFromConsumer = true
   

            self.set_lp_callbacks(command: command)

            var response:[String:String];
        
            response = ["eventName":"LPMessagingSDKInit"];
            let jsonString = self.convertDicToJSON(dic: response)

            let pluginResultInitSdk = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs:jsonString
            )

            self.commandDelegate!.send(
                pluginResultInitSdk,
                callbackId: command.callbackId
            )
            
            print("@@@ iOS LPMessagingSDKInit")

        } catch let error as NSError {
  
            var response:[String:String];
        
            response = ["eventName":"LPMessagingSDKInit"];
            let jsonString = self.convertDicToJSON(dic: response)

            let pluginResultInitSdk = CDVPluginResult(
                status: CDVCommandStatus_ERROR,
                messageAs:jsonString
            )

            self.commandDelegate!.send(
                pluginResultInitSdk,
                callbackId: command.callbackId
            )

            print("@@@ ios LPMessagingSDK Initialization error: \(error)")
        }
        
    }

    func convertToDictionary(text: String) -> [String: Any]? {
        if let data = text.data(using: .utf8) {
            do {
                return try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
            } catch {
                print(error.localizedDescription)
            }
        }
        return nil
    }
    
    func convertDeviceTokenString(token:String) -> Data {
        
        
        var result: [String] = []
        let characters = Array(token)
        stride(from: 0, to: characters.count, by: 8).forEach {
            result.append(String(characters[$0..<min($0+8, characters.count)]))
        }
        
        var tokenAsString = result.joined(separator: " ")
        
        tokenAsString = "<" + tokenAsString + ">"
        
        let tokenAsNSData = tokenAsString.hexadecimal()! as NSData
        let tokenAsData = tokenAsString.hexadecimal()!
        
        print("@@@ tokenAsNSData \(tokenAsNSData)")
        print("@@@ tokenAsData \(tokenAsData)")
        
        print("@@@ string as 8 character chunks ... \(result)")
        print("@@@ tokenAsString --> \(tokenAsString)" )
        
        return tokenAsData
    }
    
    @objc(close_conversation_screen:)
    func close_conversation_screen(command:CDVInvokedUrlCommand) {
        // self.globalCallbackCommand = command
//        if let query = self.conversationQuery {
//            let isChatActive = LPMessagingSDK.instance.checkActiveConversation(query)
//            if(isChatActive){
//                LPMessagingSDK.instance.resolveConversation(query)
//            }
//
//        }
        
        conversationScreen?.closeChat()

        var response:[String:String];
        
        response = ["eventName":"LPMessagingSDKCloseConversationScreen"];
        let jsonString = self.convertDicToJSON(dic: response)
        
        self.set_lp_callbacks(command: command)
        
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: jsonString
        )

        pluginResult?.setKeepCallbackAs(true)
        self.callBackCommandDelegate?.send(pluginResult, callbackId: self.callBackCommand?.callbackId)
        // var response:[String:String];
        // response = ["eventName":"LPMessagingSDKCloseConversationScreen"];
        // let jsonString = self.convertDicToJSON(dic: response)
        // let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:jsonString)
        // pluginResult?.setKeepCallbackAs(true)
        // self.globalCallbackCommand?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)
    }
    
    @objc(register_pusher:)
    func register_pusher(command:CDVInvokedUrlCommand) {
        // API passes in token via args object
        guard let pushToken = command.arguments[1] as? String else {
            print("Can't register pusher without device pushToken ")
            return
        }

        var convertedTokenAsData = convertDeviceTokenString(token: pushToken)
        
        // call the SDK method e.g.
        LPMessaging.instance.registerPushNotifications(token: convertedTokenAsData);
        
        self.registerLpPusherCallbackCommandDelegate = commandDelegate
        self.registerLpPusherCallbackCommand = command
        var response:[String:String];
        
        response = ["eventName":"LPMessagingSDKRegisterLpPusher","deviceToken":"\(String(describing: pushToken))"];
        
        let jsonString = self.convertDicToJSON(dic: response)
        // return NO_RESULT for now and then use this delegate in all async callbacks for other events.
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: jsonString
        )
        
        pluginResult?.setKeepCallbackAs(true)
        
        self.registerLpPusherCallbackCommandDelegate!.send(
            pluginResult,
            callbackId: self.registerLpPusherCallbackCommand!.callbackId
        )
        
    }
    
    @objc(lp_register_event_callback:)
    func lp_register_event_callback(command: CDVInvokedUrlCommand) {
        self.globalCallbackCommandDelegate = commandDelegate
        self.globalCallbackCommand = command
        
        // return NO_RESULT for now and then use this delegate in all async callbacks for other events.
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_NO_RESULT,
            messageAs: "lp_register_global_async_event_callback"
        )
        
        pluginResult?.setKeepCallbackAs(true)
        
        self.globalCallbackCommandDelegate!.send(
            pluginResult,
            callbackId: self.globalCallbackCommand!.callbackId
        )
        
        print("@@@ iOS lp_register_event_callback \n")
        
    }
    
    @objc(reconnect_with_new_token:)
    func reconnect_with_new_token(command: CDVInvokedUrlCommand) {
        
        guard let authCode = command.arguments.first as? String else {
            print("Can't init without authCode jwt")
            return
        }
        
        print("@@@ reconnect_with_new_token: new token for reconnect - \(authCode)")
        
        var response:[String:String];
        
        
        self.set_lp_callbacks(command: command)
        do {
             
            let conversationViewParams = LPConversationViewParams(conversationQuery: self.conversationQuery!, containerViewController: nil, isViewOnly: false)
            let authenticationParams = LPAuthenticationParams(authenticationCode: nil, jwt: authCode, redirectURI: nil)
            LPMessaging.instance.reconnect(self.conversationQuery!, authenticationParams: authenticationParams);
            
            response = ["eventName":"LPMessagingSDKReconnectWithNewToken","token":"\(authCode)","lpAccountNumber":"\(String(describing: lpAccountNumber))"];
            let jsonString = self.convertDicToJSON(dic: response)
            
            let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: jsonString
            )
            pluginResult?.setKeepCallbackAs(true)
            self.callBackCommandDelegate?.send(pluginResult, callbackId: self.callBackCommand?.callbackId)
        }
        catch let error as NSError {
            
            response = ["eventName":"LPMessagingSDKReconnectWithNewToken","token":"\(authCode)","lpAccountNumber":"\(String(describing: lpAccountNumber))","error":"\(error)"];
            let jsonString = self.convertDicToJSON(dic: response)

            let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_ERROR,
                messageAs: jsonString
            )
            pluginResult?.setKeepCallbackAs(true)
            self.callBackCommandDelegate?.send(pluginResult, callbackId: self.callBackCommand?.callbackId)

        }
        
    }

    @objc(lp_clear_history_and_logout:)
    func lp_clear_history_and_logout(command: CDVInvokedUrlCommand) {
        
        var response:[String:String];
        
        response = ["eventName":"LPMessagingSDKClearHistoryAndLogout"];
        let jsonString = self.convertDicToJSON(dic: response)
        
        self.set_lp_callbacks(command: command)

        LPMessaging.instance.logout(completion: {
            print("@@@ logout success!");
        }) { (error) in
            print("@@@ logout error!");
        }
        
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: jsonString
        )

        pluginResult?.setKeepCallbackAs(true)
        self.callBackCommandDelegate?.send(pluginResult, callbackId: self.callBackCommand?.callbackId)
    }
    
    @objc(start_lp_conversation:)
    func start_lp_conversation(command: CDVInvokedUrlCommand) {
        print("@@@ ios start_lp_conversation args = \(command.arguments)")
        guard let brandID = command.arguments.first as? String else {
            print("Can't start without brandID")
            return
        }

        // init our callbacks for javascript wrapper
        self.set_lp_callbacks(command: command)

        // Enable authentication support
        // check if the second parameter to the function call contains a value?
        // this is expected to be the JWT token for enabling authenticated messaging conversations
        // if found we pass it to the showConversation method, otherwise fallback to default unauthenticated mode
        var conversationType = "authenticated";

        let token = command.arguments[1] as? String ?? ""
        let partyID = command.arguments[2] as? String ?? ""
        let country = command.arguments[3] as? String ?? ""
        let region = command.arguments[4] as? String ?? ""
        let language = command.arguments[5] as? String ?? ""
        let zipcode = command.arguments[6] as? String ?? ""
        let accountName = command.arguments[7] as? String ?? ""
        let customerID = command.arguments[8] as? String ?? ""
        let ctype = command.arguments[9] as? String ?? ""
        let storedNumber = command.arguments[10] as? String ?? ""
        let entrypoint = command.arguments[11] as? String ?? ""
        let firstName = command.arguments[12] as? String ?? ""
        let lastName = command.arguments[13] as? String ?? ""
        let age = command.arguments[14] as? String ?? ""
        let year = command.arguments[15] as? String ?? ""
        let month = command.arguments[16] as? String ?? ""
        let day = command.arguments[17] as? String ?? ""
        let email = command.arguments[18] as? String ?? ""
        let phone = command.arguments[19] as? String ?? ""
        let gender = command.arguments[20] as? String ?? ""
        let company = command.arguments[21] as? String ?? ""
        let userName = command.arguments[22] as? String ?? ""

        let WelcomeMsg = command.arguments[24] as? String ?? "How can I help you today?"
        let ClearConversationMsg = command.arguments[26] as? String ?? "All of your existing conversation history will be lost. Are you sure?"
        let ClearConfirmMsg = command.arguments[27] as? String ?? "Please resolve the conversation first."
        let ChooseMsg = command.arguments[28] as? String ?? "Choose an option"
        let RevolvedTileMsg = command.arguments[29] as? String ?? "Resolve the conversation"
        let ResolvedConfirmMsg = command.arguments[30] as? String ?? "Are you sure this topic is resolved?"
        let ClearTitleMsg = command.arguments[31] as? String ?? "Clear Conversation"
        let YesMsg = command.arguments[32] as? String ?? "Yes"
        let CancelMsg = command.arguments[33] as? String ?? "Cancel"
        let ClearMsg = command.arguments[34] as? String ?? "Clear"
        let MenuMsg = command.arguments[35] as? String ?? "Menu"
        let ChatTitleHeader = command.arguments[25] as? String ?? "Visa Concierge"

        let ButtonOpt1Msg = command.arguments[36] as? String ?? "Card Billing / Loyalty"
        let ButtonOpt1Value = command.arguments[37] as? String ?? "Card Billing / Loyalty"
        let ButtonOpt2Msg = command.arguments[38] as? String ?? "Visa Concierge"
        let ButtonOpt2Value = command.arguments[39] as? String ?? "Visa Concierge"

        let LanguageChat = command.arguments[40] as? String ?? "en-UK"
        let LoadingMsg = command.arguments[41] as? String ?? "Loading..."

        self.showConversation(brandID: brandID,authenticationCode: token, partyID: partyID,country: country,region: region,language: language,zipcode: zipcode,accountName: accountName,customerID: customerID,ctype: ctype,storedNumber: storedNumber,entrypoint: entrypoint,firstName: firstName,lastName: lastName,age: age,year: year,month: month,day: day,email: email,phone: phone,gender: gender,company: company,userName: userName,WelcomeMsg: WelcomeMsg,ClearConversationMsg: ClearConversationMsg,ClearConfirmMsg: ClearConfirmMsg,ChooseMsg: ChooseMsg,RevolvedTileMsg: RevolvedTileMsg,ResolvedConfirmMsg: ResolvedConfirmMsg,ClearTitleMsg: ClearTitleMsg,YesMsg: YesMsg,CancelMsg: CancelMsg,ClearMsg: ClearMsg,MenuMsg: MenuMsg,ChatTitleHeader: ChatTitleHeader,ButtonOpt1Msg: ButtonOpt1Msg,ButtonOpt1Value: ButtonOpt1Value,ButtonOpt2Msg: ButtonOpt2Msg,ButtonOpt2Value: ButtonOpt2Value,LanguageChat: LanguageChat,LoadingMsg: LoadingMsg)

        
        var response:[String:String];
        print("@@@ LPMessagingSDKStartConversation conversationType : \(customerID)")
        
        response = ["eventName":"LPMessagingSDKStartConversation","type" : customerID];
        let jsonString = self.convertDicToJSON(dic: response)
        
               
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: jsonString
        )
        
        pluginResult?.setKeepCallbackAs(true)
        self.callBackCommandDelegate?.send(pluginResult, callbackId: self.callBackCommand?.callbackId)
       
    }

    // Assign values to our objects for triggering JS callbacks in the wrapper once native methods complete
    @objc(set_lp_callbacks:)
    func set_lp_callbacks(command: CDVInvokedUrlCommand) {
        
        self.callBackCommandDelegate = commandDelegate
        self.callBackCommand = command

    }
    
    @objc(set_lp_user_profile:)
    func set_lp_user_profile(command: CDVInvokedUrlCommand) {
        var response:[String:String];
        self.set_lp_callbacks(command: command);

        guard let brandID = command.argument(at: 0) as? String else {
            print("@@@ ios -- set_lp_user_profile ...Can't set profile without brandID")
            
            response = ["error":"set_lp_user_profile missing brandID"];
            let jsonString = self.convertDicToJSON(dic: response)
            
            let pluginResultSetUserProfileError = CDVPluginResult(
                status: CDVCommandStatus_ERROR,
                messageAs:jsonString
            )
            
            self.commandDelegate!.send(
                pluginResultSetUserProfileError,
                callbackId: command.callbackId
            )
            return
        }
        
        let firstName = command.argument(at: 1) as? String
        let lastName = command.argument(at: 2) as? String
        let nickName = command.argument(at: 3) as? String
        let profileImageURL = command.argument(at: 4) as? String
        let phoneNumber = command.argument(at: 5) as? String
        let uid = command.argument(at: 6) as? String
        let employeeID = command.argument(at: 7) as? String
        
        
        let user = LPUser(firstName: firstName, lastName: lastName, nickName: nickName,  uid: uid, profileImageURL: profileImageURL, phoneNumber: phoneNumber, employeeID: employeeID)
        
        do {
            try LPMessaging.instance.setUserProfile(user, brandID: brandID)
            
            response = ["eventName":"LPMessagingSDKSetUserProfileSuccess"];
            let jsonString = self.convertDicToJSON(dic: response)
            
            let pluginResultSetUserProfile = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs:jsonString
            )
            
            self.commandDelegate!.send(
                pluginResultSetUserProfile,
                callbackId: command.callbackId
            )

        } catch let error as NSError {
            response = ["eventName":"LPMessagingSDKSetUserProfileError","error":"\(error)"];
            let jsonString = self.convertDicToJSON(dic: response)
            
            let pluginResultSetUserProfile = CDVPluginResult(
                status: CDVCommandStatus_ERROR,
                messageAs:jsonString
            )
            
            self.commandDelegate!.send(
                pluginResultSetUserProfile,
                callbackId: command.callbackId
            )

        }
        
    }
    
    // MARK: MessagingSDK API
    /**
     Show conversation screen and use this ViewController as a container
     */
     func convertJsonToDic(json:String?)-> [[String: Any]]?{
        if let jsonStr = json{
            let data = Data(jsonStr.utf8)
            do {
                if let engagementAttributes = try JSONSerialization.jsonObject(with: data, options: []) as? [[String: Any]] {
                    return engagementAttributes
                }else {
                    return nil
                }
            } catch _ as NSError {
                return nil
            }
        }else{
            return nil
        }
    }
    func showConversation(brandID: String, authenticationCode:String? = nil, partyID:String? = nil, country:String? = nil,region:String? = nil,language:String? = nil,zipcode: String? = nil,accountName: String? = nil,customerID: String? = nil,ctype: String? = nil,storedNumber: String? = nil,entrypoint: String? = nil,firstName: String? = nil,lastName: String? = nil,age: String? = nil,year: String? = nil,month: String? = nil,day: String? = nil,email: String? = nil,phone: String? = nil,gender: String? = nil,company: String? = nil,userName: String? = nil,WelcomeMsg: String? = nil,ClearConversationMsg: String? = nil,ClearConfirmMsg: String? = nil,ChooseMsg: String? = nil,RevolvedTileMsg: String? = nil,ResolvedConfirmMsg: String? = nil,ClearTitleMsg: String? = nil,YesMsg: String? = nil,CancelMsg: String? = nil,ClearMsg: String? = nil,MenuMsg: String? = nil,ChatTitleHeader: String? = nil,ButtonOpt1Msg: String? = nil,ButtonOpt1Value: String? = nil,ButtonOpt2Msg: String? = nil,ButtonOpt2Value: String? = nil,LanguageChat: String? = nil,LoadingMsg: String? = nil) {
        
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        if let chatVC = storyboard.instantiateViewController(withIdentifier: "ConversationNavigationVC") as? UINavigationController {
            chatVC.modalPresentationStyle = .fullScreen
            if let conversationVCs = chatVC.viewControllers.first as? ConversationVC {
                conversationScreen = conversationVCs
                conversationVCs.delegate = self
                conversationVCs.conversationQuery = self.conversationQuery
                if let cgate = ChatTitleHeader{
                    conversationVCs.ChatTitleHeader = cgate
                }
                if let langs = LanguageChat{
                    conversationVCs.LanguageAPP = langs
                }
                if let welm = WelcomeMsg{
                    conversationVCs.WelcomeMsg = welm
                }
                if let loads = LoadingMsg{
                    conversationVCs.LoadingMsg = loads
                }
            }

            self.viewController.present(chatVC, animated: true, completion: nil)
            var enp = ""
            if let entryp = entrypoint{
                enp = entryp
            }
            let entryPoints = ["http://www.liveperson-test.com",enp,"lang://En"]

            let engagementAttributes = [
            [
                "type": "personal",
                "personal": [
                    "language": language,
                    "company": company,
                    "gender": gender,
                    "firstname": firstName, // FIRST NAME
                    "lastname": lastName, // SURNAME
                    "age": [
                        "age": age, // AGE AS INTEGER
                        "year": year, // BIRTH YEAR
                        "month": month, // BIRTH MONTH
                        "day": day // BIRTH DAY
                    ],
                    "contacts": [
                        [
                            "email": email,
                            "phone": phone,
                            "address": [
                                "country": country,
                                "region": region
                            ]
                        ]
                    ]
                ]
            ],
            [
                "info": [
                    "storeZipCode": zipcode,
                    "accountName": accountName,
                    "customerId": customerID,
                    "storeNumber": storedNumber,
                    "ctype": ctype,
                    "userName": userName
                ],
                "type": "ctmrinfo"
            ]
        ]
            getEngagement(entryPoints: entryPoints, engagementAttributes: engagementAttributes) { (campInfo, pageID) in

                            self.conversationQuery = LPMessaging.instance.getConversationBrandQuery(brandID, campaignInfo: campInfo)
                            if let conversationVC = chatVC.viewControllers.first as? ConversationVC {
                                conversationVC.conversationQuery = self.conversationQuery
                                //conversationVC.alert.dismiss(animated: true, completion: nil)

                                if let chdcm = ClearConversationMsg{
                                    conversationVC.ClearConversationMsg = chdcm
                                }
                                if let chooscm = ClearConfirmMsg{
                                    conversationVC.ClearConfirmMsg = chooscm
                                }
                                if let choosm = ChooseMsg{
                                    conversationVC.ChooseMsg = choosm
                                }
                                if let restm = RevolvedTileMsg{
                                    conversationVC.RevolvedTileMsg = restm
                                }
                                if let resm = ResolvedConfirmMsg{
                                    conversationVC.ResolvedConfirmMsg = resm
                                }
                                if let cleartm = ClearTitleMsg{
                                    conversationVC.ClearTitleMsg = cleartm
                                }
                                if let yesm = YesMsg{
                                    conversationVC.YesMsg = yesm
                                }
                                if let cancelm = CancelMsg{
                                    conversationVC.CancelMsg = cancelm
                                }
                                if let clearm = ClearMsg{
                                    conversationVC.ClearMsg = clearm
                                }
                                if let menuM = MenuMsg {
                                    conversationVC.MenuMsg = menuM
                                }
                                                            }
                            if authenticationCode == nil {
                                LPMessaging.instance.showConversation(self.conversationQuery!)
                            } else {
                                //let welcomeMessageParam = LPWelcomeMessage(message: WelcomeMsg, frequency: .everyConversation)

                                var Button1Msg = ""
                                var Button1Value = ""
                                var Button2Msg = ""
                                var Button2Value = ""

                                if let btn1Msg = ButtonOpt1Msg {
                                    Button1Msg = btn1Msg
                                }
                                if let btn1Value = ButtonOpt1Value {
                                    Button1Value = btn1Value
                                }
                                if let btn2Msg = ButtonOpt2Msg {
                                    Button2Msg = btn2Msg
                                }
                                if let btn2Value = ButtonOpt2Value {
                                    Button2Value = btn2Value
                                }
                                

                                
                                
                                let conversationViewParams = LPConversationViewParams(conversationQuery: self.conversationQuery!, containerViewController: chatVC.viewControllers.first, isViewOnly: false)
                                let authenticationParams = LPAuthenticationParams(authenticationCode: nil, jwt: authenticationCode, redirectURI: nil)
                                LPMessaging.instance.showConversation(conversationViewParams, authenticationParams: authenticationParams)
                            }
                       // }
            }
            
        }
        
    }
    
    private func getEngagement(entryPoints: [String], engagementAttributes: [[String:Any]], success:((LPCampaignInfo?, String?)->())?) {
        //resetting pageId and campaignInfo
        
        let monitoringParams = LPMonitoringParams(entryPoints: entryPoints, engagementAttributes: engagementAttributes)
        let identity = LPMonitoringIdentity(consumerID: nil, issuer: nil)
        LPMessaging.instance.getEngagement(identities: [identity], monitoringParams: monitoringParams, completion: { (getEngagementResponse) in
            let campaignID = getEngagementResponse.engagementDetails?.first?.campaignId
            let engagementID = getEngagementResponse.engagementDetails?.first?.engagementId
            let contextID = getEngagementResponse.engagementDetails?.first?.contextId
            let sessionID = getEngagementResponse.sessionId
            let visitorID = getEngagementResponse.visitorId
            let campaignInfo = LPCampaignInfo(campaignId: campaignID!, engagementId: engagementID!, contextId: contextID, sessionId: sessionID, visitorId: visitorID)
            let pageID = getEngagementResponse.pageId
            success?(campaignInfo, pageID)
        }) { (error) in
            success?(nil,nil)
        }
    }
    
    
    private func sendSDEwith(entryPoints: [String], engagementAttributes: [[String:Any]], pageID:String?, success:(()->())?) {
        let monitoringParams = LPMonitoringParams(entryPoints: entryPoints, engagementAttributes: engagementAttributes, pageId: pageID)
        let identity = LPMonitoringIdentity(consumerID: nil, issuer: nil)
        LPMessaging.instance.sendSDE(identities: [identity], monitoringParams: monitoringParams, completion: { (sendSdeResponse) in
            success?()
        }) { (error) in
            success?()
        }
    }
    /**
     Change default SDK configurations

     TODO: update method to support config and branding changes via a JSON object sent through via the cordova wrapper to change the settings here.

     TODO: Add support for other config options as per SDK documentation
     */
    func setSDKConfigurations() {
        let configurations = LPConfig.defaultConfiguration
        
        configurations.brandAvatarImage = UIImage(named: "agent")
        
        configurations.remoteUserBubbleBackgroundColor = UIColor.purple
        configurations.remoteUserBubbleBorderColor = UIColor.purple
        configurations.remoteUserBubbleTextColor = UIColor.white
        configurations.remoteUserAvatarIconColor = UIColor.white
        configurations.remoteUserAvatarBackgroundColor = UIColor.purple
        
        configurations.brandName = "CHAT"
        
        configurations.userBubbleBackgroundColor = UIColor.lightGray
        configurations.userBubbleTextColor = UIColor.white
        
        configurations.sendButtonEnabledColor = UIColor.purple
    }
    
    fileprivate func sendEventToJavaScript(_ dicValue:[String:String]) {
        print("@@@ ios ********* sendEventToJavaScript --> dicValue == \(dicValue)")
        
        if (self.globalCallbackCommandDelegate != nil && self.globalCallbackCommand != nil) {
            
            let jsonString = self.convertDicToJSON(dic: dicValue)
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:jsonString)
            
            pluginResult?.setKeepCallbackAs(true)
            self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)
        }
        
    }
    
    func convertDicToJSON(dic:[String:String]) -> String? {
        if let theJSONData = try? JSONSerialization.data(
            withJSONObject: dic,
            options: []) {
            let theJSONText = String(data: theJSONData,
                                     encoding: .ascii)
            return theJSONText!
        }
        return nil
    }
    
    internal func LPMessagingSDKCustomButtonTapped() {
        var response:[String:String];
        response = ["eventName":"LPMessagingSDKCustomButtonTapped"];
        let jsonString = self.convertDicToJSON(dic: response)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)
    }
    
    internal func LPMessagingSDKAgentDetails(_ agent: LPUser?) {
        var response:[String:String];
        response = ["eventName":"LPMessagingSDKAgentDetails","agent":"\(String(describing: agent))"];
        let jsonString = self.convertDicToJSON(dic: response)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)

    }
    
    internal func LPMessagingSDKActionsMenuToggled(_ toggled: Bool) {
        var response:[String:String];
        response = ["eventName":"LPMessagingSDKActionsMenuToggled","toggled":"\(toggled)"];
        let jsonString = self.convertDicToJSON(dic: response)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)
        
    }
    
    internal func LPMessagingSDKHasConnectionError(_ error: String?) {
        print("LPMessagingSDKHasConnectionError")
        var response:[String:String];
        response = ["eventName":"LPMessagingSDKHasConnectionError","error":"\(String(describing: error))"];
        let jsonString = self.convertDicToJSON(dic: response)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)

    }
    
    internal func LPMessagingSDKCSATScoreSubmissionDidFinish(_ brandID: String, rating: Int) {
        print("LPMessagingSDKCSATScoreSubmissionDidFinish: \(brandID)")
        sendEventToJavaScript([
            "eventName":"LPMessagingSDKCSATScoreSubmissionDidFinish",
            "rating" : String(rating),
            "accountId" : brandID
            ])
    }
    
    internal func LPMessagingSDKObseleteVersion(_ error: NSError) {
        var response:[String:String];
        response = ["eventName":"LPMessagingSDKObseleteVersion","error":"\(error)"];
        let jsonString = self.convertDicToJSON(dic: response)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)
    }
    
    internal func LPMessagingSDKAuthenticationFailed(_ error: NSError) {
        var response:[String:String];
        response = ["eventName":"LPMessagingSDKAuthenticationFailed","error":"\(error)"];
        let jsonString = self.convertDicToJSON(dic: response)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)
    }
    
    internal func LPMessagingSDKTokenExpired(_ brandID: String) {
        print("LPMessagingSDKTokenExpired: \(brandID)")
        var response:[String:String];
        response = ["eventName":"LPMessagingSDKTokenExpired"];
        let jsonString = self.convertDicToJSON(dic: response)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)
    }
    
    internal func LPMessagingSDKError(_ error: NSError) {
        print("LPMessagingSDKError: \(error)")
        let response = ["eventName":"LPMessagingSDKError","error":"\(error)"];
        let jsonString = self.convertDicToJSON(dic: response)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)
    }
    
    internal func LPMessagingSDKAgentIsTypingStateChanged(_ isTyping: Bool) {
        print("LPMessagingSDKAgentIsTypingStateChanged: \(isTyping)")
        var response:[String:String];
        response = ["eventName":"LPMessagingSDKAgentIsTypingState","isTyping":"\(isTyping)"];
        let jsonString = self.convertDicToJSON(dic: response)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)

    }
    

    internal func LPMessagingSDKConversationStarted(_ conversationID: String?) {
        var response:[String:String];
        response = ["eventName":"LPMessagingSDKConversationStarted","conversationID":"\(String(describing: conversationID))"];
        let jsonString = self.convertDicToJSON(dic: response)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)

    }
    
    internal func LPMessagingSDKConversationEnded(_ conversationID: String?, closeReason: LPConversationCloseReason) {
        var response:[String:String];
        
        response = ["eventName":"LPMessagingSDKConversationEnded","conversationID":"\(String(describing: conversationID))","closeReason":"\(closeReason.hashValue) \(closeReason.rawValue)"];
        let jsonString = self.convertDicToJSON(dic: response)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)
    }
    
    
    internal func LPMessagingSDKConversationCSATDismissedOnSubmittion(_ conversationID: String?) {
        print("LPMessagingSDKConversationCSATDismissedOnSubmittion: \(String(describing: conversationID))")
        let response = ["eventName":"LPMessagingSDKConversationCSATDismissedOnSubmittion","conversationID":"\(String(describing: conversationID))"];
        let jsonString = self.convertDicToJSON(dic: response)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)
    }
    
    internal func LPMessagingSDKConnectionStateChanged(_ isReady: Bool, brandID: String) {
        print("@@@ iOS ... LPMessagingSDKConnectionStateChanged: \(isReady), \(brandID)")

        
        var response:[String:String];
        
        response = ["eventName":"LPMessagingSDKConnectionStateChanged","connectionState":"\(isReady)"];
        let jsonString = self.convertDicToJSON(dic: response)
        
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)


    }
    
    internal func LPMessagingSDKOffHoursStateChanged(_ isOffHours: Bool, brandID: String) {
        print("@@@ ios... LPMessagingSDKOffHoursStateChanged: \(isOffHours), \(brandID)")
        var response:[String:String];
        
        response = ["eventName":"LPMessagingSDKOffHoursStateChanged","isOffHours":"\(isOffHours)"];
        let jsonString = self.convertDicToJSON(dic: response)
        
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)
    }
    
    internal func LPMessagingSDKConversationViewControllerDidDismiss() {
    
        print("@@@ ios ... LPMessagingSDKConversationClosed")
        var response:[String:String];
        response = ["eventName":"LPMessagingSDKConversationClosed"];
        let jsonString = self.convertDicToJSON(dic: response)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:jsonString)
        pluginResult?.setKeepCallbackAs(true)
        self.globalCallbackCommandDelegate?.send(pluginResult, callbackId: self.globalCallbackCommand?.callbackId)

        
    }
    
}


