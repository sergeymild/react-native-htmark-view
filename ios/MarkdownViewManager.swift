import React
import CoreText
import UIKit

func attributedString(
    markdownString: String
) -> NSAttributedString? {
//    if true {
//        guard let d = markdownString.data(using: .utf8) else { return nil }
//        return try! NSAttributedString.init(markdown: d)
//    }
    
    let parser = MarkdownParser()
    let htmlString = parser.html(from: markdownString)

    let str = attributedString(from: htmlString)
    return str
}



private class MarkdownTextShadowView: RCTShadowView {
    static let measure: YGMeasureFunc = { node, width, widthNode, height, heightNode in
        debugPrint("startMeasure", width, height)
        guard let context = YGNodeGetContext(node) else {
            return YGSize(width: 0, height: 0)
        }
        
        let instance = Unmanaged<MarkdownTextShadowView>.fromOpaque(context).takeUnretainedValue()
        let text = instance._content
        let maxLines = instance._maxLines
        
        let str = attributedString(markdownString: text)
        let size2 = calculateTextSize(
            for: str!,
            in: .init(
                width: CGFloat(width),
                height: CGFloat(height)),
            maxLines: maxLines == -1 ? Int.max : maxLines
        )
        
        debugPrint("endMeasure", size2)
        return YGSize(width: Float(round(size2.width)), height: Float(round(size2.height)))
    }
    
    @objc
    var _content: String = ""
    var _maxLines: Int = Int.max
    
    
    @objc
    func setParams(_ params: [String: Any]) {
        let text = RCTConvert.nsString(params["appText"])!
        self._content = text
        self._maxLines = params["maxLines"] as? Int ?? -1
        
        YGNodeMarkDirty(yogaNode)
    }
    
    override func isYogaLeafNode() -> Bool {
        return true
    }
    
    override func canHaveSubviews() -> Bool {
        return false
    }
    
    override init() {
        super.init()
        debugPrint("Init")
        YGNodeSetMeasureFunc(self.yogaNode!, SimpleTextShadowView.measure)
        YGNodeSetContext(self.yogaNode!, Unmanaged.passRetained(self).toOpaque())
    }
    
    override func layout(with layoutMetrics: RCTLayoutMetrics, layoutContext: RCTLayoutContext) {
        super.layout(with: layoutMetrics, layoutContext: layoutContext)
    }
    
    deinit {
        debugPrint("Deinit")
    }
}

@objc(MarkdownViewManager)
class MarkdownViewManager: RCTViewManager {
    override func view() -> UIView! {
        return MarkdownView(bridge: self.bridge)
    }
    
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    override func shadowView() -> RCTShadowView! {
        return SimpleTextShadowView()
    }
    
    private func getView(withTag tag: NSNumber) -> MarkdownView {
            // swiftlint:disable force_cast
            return bridge.uiManager.view(forReactTag: tag) as! MarkdownView
        }
    
    @objc
    func findLink(
        _ node: NSNumber,
        locationX: NSNumber,
        locationY: NSNumber,
        resolver: @escaping RCTPromiseResolveBlock,
        rejecter: @escaping RCTPromiseRejectBlock
    ) {
        DispatchQueue.main.async {
            debugPrint(locationX, locationY)
            let view = self.getView(withTag: node)
            let link = view.findLink(.init(
                x: CGFloat(locationX.doubleValue),
                y: CGFloat(locationY.doubleValue)
            ))
            resolver(link)
        }
    }
}

private class MarkdownView : UILabel, UIGestureRecognizerDelegate {
    
    init(bridge: RCTBridge) {
        super.init(frame: .zero)
        numberOfLines = 0
        textColor = .black
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc
    func setParams(_ params: [String: Any]) {
        let text = RCTConvert.nsString(params["appText"])!
        let attrStr = attributedString(markdownString: text)
        self.attributedText = attrStr
        self.numberOfLines = params["maxLines"] as? Int ?? Int.max
        if let ellipsize = RCTConvert.nsString(params["ellipsize"]) {
            switch ellipsize {
            case "tail":
                self.lineBreakMode = .byTruncatingTail
            case "head":
                self.lineBreakMode = .byTruncatingHead
            case "middle":
                self.lineBreakMode = .byTruncatingMiddle
            case "clip":
                self.lineBreakMode = .byClipping
            default:
                self.lineBreakMode = .byWordWrapping
            }
        }
    }
    
    deinit {
        debugPrint("Deinit2")
    }
    

    func findLink(_ location: CGPoint) -> String? {
        let textStorage = NSTextStorage(attributedString: self.attributedText!)
        let layoutManager = NSLayoutManager()
        let textContainer = NSTextContainer(size: self.bounds.size)
        layoutManager.addTextContainer(textContainer)
        textStorage.addLayoutManager(layoutManager)
        
        let textBoundingRect = layoutManager.boundingRect(forGlyphRange: NSRange(location: 0, length: textStorage.length), in: textContainer)
        let textViewFrame = textBoundingRect.offsetBy(dx: self.frame.origin.x, dy: self.frame.origin.y)
        
        if textViewFrame.contains(location) {
            let characterIndex = layoutManager.characterIndex(for: location, in: textContainer, fractionOfDistanceBetweenInsertionPoints: nil)
            if let url = (self.attributedText?.attribute(.link, at: characterIndex, effectiveRange: nil) as? Foundation.URL) {
                return url.absoluteString
            }
        }
        return nil
    }
}
