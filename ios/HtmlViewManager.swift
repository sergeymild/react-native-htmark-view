import React
import CoreText
import UIKit

func attributedString(
    from htmlString: String
) -> NSAttributedString? {
    let modifiedHTML = """
        <style>
        body, p, ul, ol, li, div, h1, h2, h3, h4, h5, h6 {
            margin: 0 !important;
            padding: 0 !important;
        }
        p { display: inline-block; }
        </style>
        \(htmlString)
        """
    
    debugPrint(modifiedHTML)
    debugPrint("=====================")
    
    guard let data = modifiedHTML.data(using: .utf8) else { return nil }
    
    do {
        let attributedString = try NSAttributedString(
            data: data,
            options: [
                .documentType: NSAttributedString.DocumentType.html,
                .characterEncoding: String.Encoding.utf8.rawValue
            ],
            documentAttributes: nil)
        let str = NSMutableAttributedString(attributedString: attributedString)
        return str
    } catch {
        print("Error creating attributed string: \(error)")
        return nil
    }
}

func calculateBoundingRect(
    for attributedString: NSAttributedString,
    size: CGSize,
    maxLines: Int
) -> CGSize {
    let framesetter = CTFramesetterCreateWithAttributedString(attributedString as CFAttributedString)
    let constrainedSize = size
    let textPath = CGPath(rect: CGRect(origin: .zero, size: constrainedSize), transform: nil)
    let frame = CTFramesetterCreateFrame(framesetter, CFRange(location: 0, length: 0), textPath, nil)
    
    let lines = CTFrameGetLines(frame) as! [CTLine]
    let lineCount = min(lines.count, maxLines)
    // Get the heights of the lines
    
    var linesHeight: CGFloat = 0
    var textWidth: CGFloat = 0
    
    for index in (0..<lineCount) {
        let line = lines[index]
        var ascent: CGFloat = 0
        var descent: CGFloat = 0
        var leading: CGFloat = 0
        let lineWidth = CTLineGetTypographicBounds(line, &ascent, &descent, &leading)
        linesHeight += ascent + descent + leading
        textWidth = max(textWidth, lineWidth + leading)
    }
    
    debugPrint(textWidth, linesHeight)
    
    return .init(width: min(textWidth, size.width), height: min(linesHeight, size.height))
}

func calculateTextSize(
    for attributedString: NSAttributedString,
    in size: CGSize,
    maxLines: Int
) -> CGSize {
    return calculateBoundingRect(for: attributedString, size: size, maxLines: maxLines)
    // Create a UILabel for measuring text
    let label = UILabel()
    label.numberOfLines = maxLines
    label.lineBreakMode = .byTruncatingTail
    label.attributedText = attributedString
    label.frame.size.width = size.width
    
    // Ensure the label height is constrained to fit within the maximum number of lines
    let maxHeight = label.font.lineHeight * CGFloat(maxLines)
    label.frame.size.height = maxHeight
    
    // Size the label's text to fit within the constraints
    label.sizeToFit()
    
    // Return the calculated bounding rect
    var boundingRect = label.frame
    boundingRect.size.height = min(boundingRect.size.height, maxHeight)
    
    return boundingRect.size
}

class SimpleTextShadowView: RCTShadowView {
    static let measure: YGMeasureFunc = { node, width, widthNode, height, heightNode in
        debugPrint("startMeasure", width, height)
        guard let context = YGNodeGetContext(node) else {
            return YGSize(width: 0, height: 0)
        }
        
        let instance = Unmanaged<SimpleTextShadowView>.fromOpaque(context).takeUnretainedValue()
        let text = instance._content
        let isMark = instance._isMark
        let maxLines = instance._maxLines
        
        let str = isMark ? attributedString(markdownString: text) : attributedString(from: text)
        let size2 = calculateTextSize(
            for: str!,
            in: .init(
                width: CGFloat(width),
                height: CGFloat(height)),
            maxLines: maxLines == -1 ? Int.max : maxLines
        )
        
        debugPrint("endMeasure", size2)
        return YGSize(width: Float(size2.width), height: Float(size2.height))
    }
    
    @objc
    var _content: String = ""
    var _isMark = false
    var _maxLines: Int = Int.max
    
    
    @objc
    func setParams(_ params: [String: Any]) {
        if let text = RCTConvert.nsString(params["appText"]) {
            _isMark = false
            self._content = text
        } else if let text = RCTConvert.nsString(params["markdown"]) {
            _isMark = true
            self._content = text
        }
        
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

@objc(HtmlViewManager)
class HtmlViewManager: RCTViewManager {
    override func view() -> UIView! {
        return HtmlView(bridge: self.bridge)
    }
    
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    override func shadowView() -> RCTShadowView! {
        return SimpleTextShadowView()
    }
    
    private func getView(withTag tag: NSNumber) -> HtmlView {
            // swiftlint:disable force_cast
            return bridge.uiManager.view(forReactTag: tag) as! HtmlView
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
    
    @objc
    func findLinkSync(
        _ node: NSNumber,
        locationX: NSNumber,
        locationY: NSNumber
    ) -> String? {
        var result: String?
        DispatchQueue.main.sync {
            debugPrint(locationX, locationY)
            let view = self.getView(withTag: node)
            let link = view.findLink(.init(
                x: CGFloat(locationX.doubleValue),
                y: CGFloat(locationY.doubleValue)
            ))
            result = link
        }
        return result
    }
}

class HtmlView : UILabel, UIGestureRecognizerDelegate {
    
    init(bridge: RCTBridge) {
        super.init(frame: .zero)
        numberOfLines = 0
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc
    func setParams(_ params: [String: Any]) {
        if let text = RCTConvert.nsString(params["appText"]) {
            let attrStr = attributedString(from: text)
            self.attributedText = attrStr
        } else if let text = RCTConvert.nsString(params["markdown"]) {
            let attrStr = attributedString(markdownString: text)
            self.attributedText = attrStr
        }
        
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
