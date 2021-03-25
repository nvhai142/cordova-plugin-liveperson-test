//
//  ConversationVC.swift
//  LinhTinhSwift
//
//  Created by Hoan Nguyen on 7/1/20.
//  Copyright © 2020 Hoan Nguyen. All rights reserved.
//

import UIKit
import LPMessagingSDK

import UserNotifications


class ConversationVC: UIViewController, LPMessagingSDKdelegate {
    var delegate:ConversationDelegate?
    
    var conversationQuery:ConversationParamProtocol?;
    var alert = UIAlertController(title: nil, message: "Loading...", preferredStyle: .alert)

    var WelcomeMsg:String = "How can I help you today?"
    var ClearConversationMsg:String = "All of your existing conversation history will be lost. Are you sure?"
    var ClearConfirmMsg:String = "Please resolve the conversation first."
    var ChooseMsg:String = "Choose an option"
    var RevolvedTileMsg:String = "Resolve the conversation"
    var ResolvedConfirmMsg:String = "Are you sure this topic is resolved?"
    var ClearTitleMsg:String = "Clear Conversation"
    var YesMsg:String = "Yes"
    var CancelMsg:String = "Cancel"
    var ClearMsg:String = "Clear"
    var MenuMsg:String = "Menu"
    var ChatTitleHeader:String = "Visa Concierge"
    var LanguageAPP:String = "en-UK"
    var LoadingMsg:String = "Loading..."

    var backgroundDate:NSDate?
    @objc func appDidEnterBackground() {
        backgroundDate = NSDate()
    }
    
    var kTimeoutUserInteraction: Double = 15*60
    var idleTimer:Timer?
    @objc func resetIdleTimer() {
        if (idleTimer == nil) {
            idleTimer = Timer(timeInterval: 15*60, target: self, selector: #selector(idleTimerExceeded), userInfo: nil, repeats: false)
            RunLoop.current.add(idleTimer!, forMode: .default)
        } else {
            if (fabs(idleTimer!.fireDate.timeIntervalSinceNow) < (kTimeoutUserInteraction - 1.0)) {
                idleTimer!.fireDate = Date(timeIntervalSinceNow: kTimeoutUserInteraction)
            }
        }
    }
    
    @objc func idleTimerExceeded() {
        idleTimer = nil
        // làm cái bếp gì đó đây nha
        self.closeChat()
    }
    override var next: UIResponder?{
        get {
            self.resetIdleTimer()
            return super.next
        }
    }

    @objc func appWillEnterForeground() {
        let now = NSDate()
        if let oldDate = backgroundDate{
            if (oldDate.timeIntervalSinceReferenceDate + kTimeoutUserInteraction) < now.timeIntervalSinceReferenceDate
            {
                self.closeChat()
                return
            }
        }
        print( "")
    }
    
    func addAppObserver(){
        NotificationCenter.default.addObserver(self, selector: #selector(appDidEnterBackground), name: UIApplication.didEnterBackgroundNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(appWillEnterForeground), name: UIApplication.willEnterForegroundNotification, object: nil)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        LPMessaging.instance.delegate = self
        self.configUI()

        self.addAppObserver()

        let loadingIndicator = UIActivityIndicatorView(frame: CGRect(x: 10, y: 5, width: 50, height: 50))
        loadingIndicator.hidesWhenStopped = true
        loadingIndicator.style = UIActivityIndicatorView.Style.gray
        //loadingIndicator.activityIndicatorViewStyle = UIActivityIndicatorViewStyle.whiteLarge
        //loadingIndicator.color = .gray
        loadingIndicator.startAnimating();
        
        alert.message = LoadingMsg

        alert.view.addSubview(loadingIndicator)
        present(alert, animated: true, completion: nil)

        self.resetIdleTimer()
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(resetIdleTimer))
        tapGesture.cancelsTouchesInView = false
        self.navigationController?.view.addGestureRecognizer(tapGesture)
    }

    func setupLanguage(language:String){
        let configL = LPConfig.defaultConfiguration
        switch language {
        case "zh-HK":
            configL.language = LPLanguage.zh_Hant_hk
            break
        case "ja-JP":
            configL.language = LPLanguage.ja
            break
        case "zh-TW":
            configL.language = LPLanguage.zh
            break
        case "ko-KR":
            configL.language = LPLanguage.ko
            break
        case "en-UK":
            configL.language = LPLanguage.en
            break
        default:
            configL.language = LPLanguage.ko
        }
    }
    
    func configUI() {
        self.navigationController?.navigationBar.tintColor = UIColor.white
        self.navigationController?.navigationBar.barTintColor = UIColor.csatNavigationBackgroundColor
        self.navigationController?.navigationBar.isTranslucent = false
        self.navigationController?.navigationBar.titleTextAttributes = [NSAttributedString.Key.foregroundColor : UIColor.white];
        self.title = ChatTitleHeader
        self.view.backgroundColor = UIColor.conversationBackgroundColor
        
        let configUI = LPConfig.defaultConfiguration

        switch LanguageAPP {
        case "zh-HK":
            configUI.language = LPLanguage.zh_Hant_hk
            break
        case "ja-JP":
            configUI.language = LPLanguage.ja
            break
        case "zh-TW":
            configUI.language = LPLanguage.zh_Hant_hk
            break
        case "ko-KR":
            configUI.language = LPLanguage.ko
            break
        case "en-UK":
            configUI.language = LPLanguage.en
            break
        default:
            configUI.language = LPLanguage.en
        }

        configUI.fileSharingFromAgent = true
        configUI.fileSharingFromConsumer = true

        configUI.conversationBackgroundColor = UIColor.conversationBackgroundColor
        
        configUI.userBubbleBackgroundColor = UIColor.userBubbleBackgroundColor
        configUI.userBubbleBorderColor = UIColor.userBubbleBorderColor
        configUI.userBubbleLinkColor = UIColor.userBubbleLinkColor
        configUI.userBubbleTextColor = UIColor.userBubbleTextColor
        configUI.userBubbleTimestampColor =  UIColor.userBubbleTimestampColor
        configUI.userBubbleSendStatusTextColor = UIColor.userBubbleSendStatusTextColor
        configUI.userBubbleErrorTextColor = UIColor.userBubbleErrorTextColor
        configUI.userBubbleErrorBorderColor = UIColor.userBubbleErrorBorderColor
        configUI.userBubbleLongPressOverlayColor = UIColor.userBubbleLongPressOverlayColor

        configUI.remoteUserBubbleBackgroundColor = UIColor.remoteUserBubbleBackgroundColor
        configUI.remoteUserBubbleBorderColor = UIColor.remoteUserBubbleBorderColor
        configUI.remoteUserBubbleLinkColor = UIColor.remoteUserBubbleLinkColor
        configUI.remoteUserBubbleTextColor = UIColor.remoteUserBubbleTextColor
        configUI.remoteUserBubbleTimestampColor = UIColor.remoteUserBubbleTimestampColor
        configUI.remoteUserTypingTintColor = UIColor.remoteUserTypingTintColor
        configUI.remoteUserBubbleLongPressOverlayColor = UIColor.remoteUserBubbleLongPressOverlayColor

        configUI.linkPreviewBackgroundColor = UIColor.linkPreviewBackgroundColor
        configUI.linkPreviewTitleTextColor = UIColor.linkPreviewTitleTextColor
        configUI.linkPreviewDescriptionTextColor = UIColor.linkPreviewDescriptionTextColor
        configUI.linkPreviewSiteNameTextColor = UIColor.linkPreviewSiteNameTextColor
        configUI.urlRealTimePreviewBackgroundColor = UIColor.urlRealTimePreviewBackgroundColor
        configUI.urlRealTimePreviewBorderColor = UIColor.urlRealTimePreviewBorderColor
        configUI.urlRealTimePreviewTitleTextColor = UIColor.urlRealTimePreviewTitleTextColor
        configUI.urlRealTimePreviewDescriptionTextColor = UIColor.urlRealTimePreviewDescriptionTextColor

        configUI.inputTextViewContainerBackgroundColor = UIColor.inputTextViewContainerBackgroundColor

        configUI.photosharingMenuBackgroundColor = UIColor.photosharingMenuBackgroundColor
        configUI.photosharingMenuButtonsBackgroundColor = UIColor.photosharingMenuButtonsBackgroundColor
        configUI.photosharingMenuButtonsTintColor = UIColor.photosharingMenuButtonsTintColor
        configUI.photosharingMenuButtonsTextColor = UIColor.photosharingMenuButtonsTextColor
        configUI.cameraButtonEnabledColor = UIColor.cameraButtonEnabledColor
        configUI.cameraButtonDisabledColor = UIColor.cameraButtonDisabledColor
        configUI.fileCellLoaderFillColor = UIColor.fileCellLoaderFillColor
        configUI.fileCellLoaderRingProgressColor = UIColor.fileCellLoaderRingProgressColor
        configUI.fileCellLoaderRingBackgroundColor = UIColor.fileCellLoaderRingBackgroundColor
        configUI.isReadReceiptTextMode = false
        configUI.checkmarkVisibility = .sentOnly
        configUI.csatShowSurveyView = false

        configUI.ttrBannerBackgroundColor = UIColor.ttrBannerBackgroundColor
        configUI.ttrBannerTextColor = UIColor.ttrBannerTextColor

        configUI.dateSeparatorBackgroundColor = UIColor.dateSeparatorBackgroundColor
        configUI.dateSeparatorTitleBackgroundColor = UIColor.dateSeparatorTitleBackgroundColor
        configUI.dateSeparatorTextColor = UIColor.dateSeparatorTextColor
        configUI.dateSeparatorLineBackgroundColor = UIColor.dateSeparatorLineBackgroundColor
        configUI.conversationSeparatorTextColor = UIColor.conversationSeparatorTextColor
        configUI.conversationSeparatorFontName = "HelveticaNeue-Bold"
    }
    
    @IBAction func cancelPressed(sender:Any) {
        if self.conversationQuery != nil {
            LPMessaging.instance.removeConversation(self.conversationQuery!)
        }
        NotificationCenter.default.removeObserver(self, name:UIApplication.didEnterBackgroundNotification , object: nil)
        NotificationCenter.default.removeObserver(self, name: UIApplication.willEnterForegroundNotification, object: nil)
        self.dismiss(animated: true, completion: nil)
    }
    
    
    public func closeChat(){
        // if let query = self.conversationQuery {
        //     let isChatActive = LPMessagingSDK.instance.checkActiveConversation(query)
        //     if isChatActive{
                
        //     }else{
        //         NotificationCenter.default.removeObserver(self, name:UIApplication.didEnterBackgroundNotification , object: nil)
        //         NotificationCenter.default.removeObserver(self, name: UIApplication.willEnterForegroundNotification, object: nil)
        //         self.dismiss(animated: true, completion: nil)
        //     }
        // }
        if self.conversationQuery != nil {
            LPMessaging.instance.removeConversation(self.conversationQuery!)
        }
        NotificationCenter.default.removeObserver(self, name:UIApplication.didEnterBackgroundNotification , object: nil)
        NotificationCenter.default.removeObserver(self, name: UIApplication.willEnterForegroundNotification, object: nil)
        self.dismiss(animated: true, completion: nil)
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        idleTimer = nil
    }

    @IBAction func optionPressed(sender:Any) {
        if let query = self.conversationQuery {
            let isChatActive = LPMessaging.instance.checkActiveConversation(query)
            
            func showResolveConfirmation(title:String, message:String){
                let confirmAlert = UIAlertController(title: title, message: message, preferredStyle: .alert)
                confirmAlert.addAction(UIAlertAction(title: YesMsg, style: .default, handler: { (alertAction) in
                    LPMessaging.instance.resolveConversation(query)
                }))
                confirmAlert.addAction(UIAlertAction(title: CancelMsg, style: .cancel, handler: nil))
                self.present(confirmAlert, animated: true, completion: nil)
            }
            
            func showClearConfirmation(){
                let clearAlert = UIAlertController(title: ClearTitleMsg, message: ClearConversationMsg, preferredStyle: .alert)
                clearAlert.addAction(UIAlertAction(title: ClearMsg, style: .default, handler: { (alertAction) in
                    if isChatActive{
                        showResolveConfirmation(title: self.ClearTitleMsg, message: self.ClearConfirmMsg)
                    }else {
                       try? LPMessaging.instance.clearHistory(query)
                    }
                }))
                clearAlert.addAction(UIAlertAction(title: CancelMsg, style: .cancel, handler: nil))
                self.present(clearAlert, animated: true, completion: nil)
            }
            
            let st = UIStoryboard()
            st.instantiateInitialViewController()
            
            let alertVC = UIAlertController(title: MenuMsg, message: ChooseMsg, preferredStyle: .actionSheet)
            
            
            let resolveAction = UIAlertAction(title: RevolvedTileMsg, style: .default) { (alertAction) in
                showResolveConfirmation(title: self.RevolvedTileMsg, message: self.ResolvedConfirmMsg)
            }
            
            let clearHistoryAction = UIAlertAction(title: ClearTitleMsg, style: .default) { (alertAction) in
                showClearConfirmation()
            }
            
            let cancelAction = UIAlertAction(title: CancelMsg, style: .cancel, handler: nil)
            
            alertVC.addAction(resolveAction)
            alertVC.addAction(clearHistoryAction)
            alertVC.addAction(cancelAction)
            
            resolveAction.isEnabled = isChatActive;
            self.present(alertVC, animated: true, completion: nil)
        }
    }
    
    func LPMessagingSDKObseleteVersion(_ error: NSError) {
        self.delegate?.LPMessagingSDKObseleteVersion(error)
    }
    
    func LPMessagingSDKAuthenticationFailed(_ error: NSError) {
        DispatchQueue.main.async {
            self.alert.dismiss(animated: true, completion: nil)
        }
        self.delegate?.LPMessagingSDKAuthenticationFailed(error)
    }
    
    func LPMessagingSDKTokenExpired(_ brandID: String) {
        DispatchQueue.main.async {
            self.alert.dismiss(animated: true, completion: nil)
        }
        self.delegate?.LPMessagingSDKTokenExpired(brandID)
    }
    
    func LPMessagingSDKError(_ error: NSError) {
        DispatchQueue.main.async {
            self.alert.dismiss(animated: true, completion: nil)
        }
        self.delegate?.LPMessagingSDKError(error)
    }
    
    func LPMessagingSDKAgentDetails(_ agent: LPUser?) {
        if let user = agent{
            self.title = (user.nickName ?? ChatTitleHeader)
        }
        self.delegate?.LPMessagingSDKAgentDetails(agent)
    }
    
    func LPMessagingSDKConnectionStateChanged(_ isReady: Bool, brandID: String) {
        if(isReady){
            DispatchQueue.main.async {
                self.alert.dismiss(animated: true, completion: nil)
            }
        }
        self.delegate?.LPMessagingSDKConnectionStateChanged(isReady, brandID: brandID)
    }
    func LPMessagingSDKCustomButtonTapped(){
        self.delegate?.LPMessagingSDKCustomButtonTapped()
    }
    
    func LPMessagingSDKActionsMenuToggled(_ toggled: Bool) {
        self.delegate?.LPMessagingSDKActionsMenuToggled(toggled)
    }
    
    func LPMessagingSDKHasConnectionError(_ error: String?) {
        self.delegate?.LPMessagingSDKHasConnectionError(error)
    }
    
    func LPMessagingSDKCSATScoreSubmissionDidFinish(_ brandID: String, rating: Int) {
        self.delegate?.LPMessagingSDKCSATScoreSubmissionDidFinish(brandID, rating: rating)
    }
    
    func LPMessagingSDKAgentIsTypingStateChanged(_ isTyping: Bool) {
        self.delegate?.LPMessagingSDKAgentIsTypingStateChanged(isTyping)
    }
    
    func LPMessagingSDKConversationStarted(_ conversationID: String?) {
        self.delegate?.LPMessagingSDKConversationStarted(conversationID)
    }
    
    func LPMessagingSDKConversationEnded(_ conversationID: String?, closeReason: LPConversationCloseReason) {
        self.delegate?.LPMessagingSDKConversationEnded(conversationID, closeReason: closeReason)
    }
    
    func LPMessagingSDKConversationCSATDismissedOnSubmittion(_ conversationID: String?) {
        self.delegate?.LPMessagingSDKConversationCSATDismissedOnSubmittion(conversationID)
    }
    
    func LPMessagingSDKOffHoursStateChanged(_ isOffHours: Bool, brandID: String) {
        self.delegate?.LPMessagingSDKOffHoursStateChanged(isOffHours, brandID: brandID)
    }
    
    func LPMessagingSDKConversationViewControllerDidDismiss() {
        self.delegate?.LPMessagingSDKConversationViewControllerDidDismiss()
    }
}



public protocol ConversationDelegate {
    func LPMessagingSDKCustomButtonTapped()

    func LPMessagingSDKAgentDetails(_ agent: LPUser?)
    
    func LPMessagingSDKActionsMenuToggled(_ toggled: Bool)
    
    func LPMessagingSDKHasConnectionError(_ error: String?)
    
    func LPMessagingSDKCSATScoreSubmissionDidFinish(_ brandID: String, rating: Int)
    
    func LPMessagingSDKObseleteVersion(_ error: NSError)
    
    func LPMessagingSDKAuthenticationFailed(_ error: NSError)
    
    func LPMessagingSDKTokenExpired(_ brandID: String)
    
    func LPMessagingSDKError(_ error: NSError)
    
    func LPMessagingSDKAgentIsTypingStateChanged(_ isTyping: Bool)

    func LPMessagingSDKConversationStarted(_ conversationID: String?)
    
    func LPMessagingSDKConversationEnded(_ conversationID: String?, closeReason: LPConversationCloseReason)
    
    func LPMessagingSDKConversationCSATDismissedOnSubmittion(_ conversationID: String?)
    
    func LPMessagingSDKConnectionStateChanged(_ isReady: Bool, brandID: String)
    
    func LPMessagingSDKOffHoursStateChanged(_ isOffHours: Bool, brandID: String)
    
    func LPMessagingSDKConversationViewControllerDidDismiss()
}
