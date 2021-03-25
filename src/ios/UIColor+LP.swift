//
//  UIColor+LP.swift
//  LinhTinhSwift
//
//  Created by Hoan Nguyen on 7/1/20.
//  Copyright © 2020 Hoan Nguyen. All rights reserved.
//

import Foundation
import UIKit

extension UIColor{
    
    static let backgroundColor = UIColor(hex: "#FFFFFF")

    static let conversationBackgroundColor = UIColor(hex: "#FFFFFF")

    static let remoteUserBubbleBackgroundColor = UIColor(hex: "#CAD1D6")
    static let remoteUserBubbleBorderColor = UIColor(hex: "#CAD1D6")
    static let remoteUserBubbleLinkColor = UIColor(hex: "#152B55")
    static let remoteUserBubbleTextColor = UIColor(hex: "#636363")
    static let remoteUserBubbleTimestampColor = UIColor(hex: "##767676")
    static let remoteUserTypingTintColor = UIColor(hex: "#636363")
    static let remoteUserBubbleLongPressOverlayColor = UIColor(hex: "#C1C1C1")

    static let userBubbleBackgroundColor = UIColor(hex: "#54606C")
    static let userBubbleBorderColor = UIColor(hex: "#54606C")
    static let userBubbleLinkColor = UIColor(hex: "#FFFFFF")
    static let userBubbleTextColor = UIColor(hex: "#FFFFFF")
    static let userBubbleTimestampColor = UIColor(hex: "##767676")
    static let userBubbleSendStatusTextColor = UIColor(hex: "#D7D8D6")
    static let userBubbleErrorTextColor = UIColor(hex: "#DB0011")
    static let userBubbleErrorBorderColor = UIColor(hex: "#DB0011")
    static let userBubbleLongPressOverlayColor = UIColor(hex: "#F5F5F5")

    static let linkPreviewBackgroundColor = UIColor(hex: "#152B55")
    static let linkPreviewTitleTextColor = UIColor(hex: "#FFFFFF")
    static let linkPreviewDescriptionTextColor = UIColor(hex: "#F5F5F5")
    static let linkPreviewSiteNameTextColor = UIColor(hex: "#767676")

    static let urlRealTimePreviewBackgroundColor = UIColor(hex: "#FFFFFF")
    static let urlRealTimePreviewBorderColor = UIColor(hex: "#C1C1C1")

    static let urlRealTimePreviewTitleTextColor = UIColor(hex: "#767676")
    static let urlRealTimePreviewDescriptionTextColor = UIColor(hex: "#767676")

    static let photosharingMenuBackgroundColor = UIColor(hex: "#152B55")
    static let photosharingMenuButtonsBackgroundColor = UIColor(hex: "#FFFFFF")
    static let photosharingMenuButtonsTintColor = UIColor(hex: "#152B55")
    static let photosharingMenuButtonsTextColor = UIColor(hex: "#FFFFFF")
    static let cameraButtonEnabledColor = UIColor(hex: "#152B55")
    static let cameraButtonDisabledColor = UIColor(hex: "#F5F5F5")
    static let fileCellLoaderFillColor = UIColor(hex: "#00CA3D")
    static let fileCellLoaderRingProgressColor = UIColor(hex: "#00CA3D")
    static let fileCellLoaderRingBackgroundColor = UIColor(hex: "#FFFFFF")


    static let sendButtonDisabledColor = UIColor(hex: "#F5F5F5")
    static let sendButtonEnabledColor = UIColor(hex: "#152B55")
    static let systemBubbleTextColor = UIColor(hex: "#1C1C1C")

    static let checkmarkReadColor = UIColor(hex: "#767676")
    static let checkmarkDistributedColor = UIColor(hex: "#767676")
    static let checkmarkSentColor = UIColor(hex: "#767676")

    static let csatSubmitButtonBackgroundColor = UIColor(hex: "#FFFFFF")
    static let csatSubmitButtonTextColor = UIColor(hex: "#000000")
    static let csatRatingButtonSelectedColor = UIColor(hex: "#FFFFFF")
    static let csatAllTitlesTextColor = UIColor(hex: "#000000")

    static let csatNavigationBackgroundColor = UIColor(hex: "#152B55")
    static let csatNavigationTitleColor = UIColor(hex: "#000000")
    static let csatSkipButtonColor = UIColor(hex: "#000000")
    static let conversationSeparatorTextColor = UIColor(hex: "#163049")

    static let dateSeparatorTitleBackgroundColor = UIColor(hex: "#FFFFFF")
    static let dateSeparatorTextColor = UIColor(hex: "#636363")
    static let dateSeparatorLineBackgroundColor = UIColor(hex: "#FFFFFF")
    static let dateSeparatorBackgroundColor = UIColor(hex: "#FFFFFF")

    static let inputTextViewContainerBackgroundColor = UIColor(hex: "#F5F5F5")

    static let ttrBannerBackgroundColor = UIColor(hex: "#636363")
    static let ttrBannerTextColor = UIColor(hex: "#FFFFFF")

    static let remoteUserAvatarBackgroundColor = UIColor(hex: "#D7D8D6")
    static let remoteUserAvatarIconColor = UIColor(hex: "#000000")
    static let csatAgentAvatarBackgroundColor = UIColor(hex: "#D7D8D6")
    static let csatAgentAvatarIconColor = UIColor(hex: "#000000")

    static let conversationNavigationBackgroundColor = UIColor(hex: "#FFFFFF")
    static let conversationNavigationTitleColor = UIColor(hex: "#000000")
    static let secureFormBackButtonColor = UIColor(hex: "#000000")

    static let secureFormNavigationBackgroundColor = UIColor(hex: "#FFFFFF")
    static let secureFormNavigationTitleColor = UIColor(hex: "#000000")
    static let secureFormBubbleBackgroundColor = UIColor(hex: "#FFFFFF")
    static let secureFormBubbleBorderColor = UIColor(hex: "#D7D8D6")
    static let secureFormBubbleTitleColor = UIColor(hex: "#D7D8D6")
    static let secureFormBubbleDescriptionColor = UIColor(hex: "#767676")
    static let secureFormBubbleFillFormButtonTextColor = UIColor(hex: "#767676")
    static let secureFormBubbleFillFormButtonBackgroundColor = UIColor(hex: "#FFFFFF")
    static let secureFormBubbleFormImageTintColor = UIColor(hex: "#152B55")

    static let connectionStatusConnectingBackgroundColor = UIColor(hex: "#D7D8D6")
    static let connectionStatusConnectingTextColor = UIColor(hex: "#636363")
    static let connectionStatusFailedToConnectBackgroundColor = UIColor(hex: "#D7D8D6")
    static let connectionStatusFailedToConnectTextColor = UIColor(hex: "#152B55")
    static let controllerBubbleTextColor = UIColor(hex: "#000000")
}



extension UIColor {
     convenience init(hex: String) {
           var hexSanitized = hex.trimmingCharacters(in: .whitespacesAndNewlines)
           hexSanitized = hexSanitized.replacingOccurrences(of: "#", with: "")

           var rgb: UInt32 = 0

           var r: CGFloat = 0.0
           var g: CGFloat = 0.0
           var b: CGFloat = 0.0
           var a: CGFloat = 1.0

           let length = hexSanitized.count

        Scanner(string: hexSanitized).scanHexInt32(&rgb)

           if length == 6 {
               r = CGFloat((rgb & 0xFF0000) >> 16) / 255.0
               g = CGFloat((rgb & 0x00FF00) >> 8) / 255.0
               b = CGFloat(rgb & 0x0000FF) / 255.0

           } else if length == 8 {
               r = CGFloat((rgb & 0xFF000000) >> 24) / 255.0
               g = CGFloat((rgb & 0x00FF0000) >> 16) / 255.0
               b = CGFloat((rgb & 0x0000FF00) >> 8) / 255.0
               a = CGFloat(rgb & 0x000000FF) / 255.0

           }

           self.init(red: r, green: g, blue: b, alpha: a)
       }
       static var customAccent: UIColor {
        if #available(iOS 13, *) {
            return UIColor { (traitCollection: UITraitCollection) -> UIColor in
                if traitCollection.userInterfaceStyle == .dark {
                    return UIColor.black
                } else {
                    return UIColor.white
                }
            }
        } else {
            return UIColor.white
        }
    }
}
