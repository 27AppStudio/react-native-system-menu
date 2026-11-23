import SwiftUI
import UIKit

@objc(RNContextMenuItem)
public final class ContextMenuItem: NSObject {
  let id: String
  let title: String
  let systemImage: String
  let destructive: Bool

  @objc public init(id: String, title: String, systemImage: String, destructive: Bool) {
    self.id = id
    self.title = title
    self.systemImage = systemImage
    self.destructive = destructive
  }
}

final class ContextMenuModel: ObservableObject {
  @Published var items: [ContextMenuItem] = []
  @Published var cornerRadius: CGFloat = 0
  var onSelect: ((String) -> Void)?
}

/// Puts SwiftUI's `.contextMenu` around React Native's children.
///
/// The children are mounted in `container`, which SwiftUI shows as a representable: UIKit and
/// SwiftUI add their own views around it, never among the children Fabric mounts by index.
@objc(RNContextMenuHost)
public final class ContextMenuHost: NSObject {
  @objc public let container = UIView()
  @objc public var view: UIView { controller.view }
  @objc public var onSelect: ((String) -> Void)? {
    get { model.onSelect }
    set { model.onSelect = newValue }
  }

  private let model = ContextMenuModel()
  private let controller: UIHostingController<ContextMenuContent>

  @objc override public init() {
    controller = UIHostingController(rootView: ContextMenuContent(model: model, container: container))
    super.init()
    controller.view.backgroundColor = .clear
    if #available(iOS 16.4, *) {
      // The card's own bounds, also under a bar or the home indicator.
      controller.safeAreaRegions = []
    }
  }

  @objc public func update(items: [ContextMenuItem], cornerRadius: CGFloat) {
    model.items = items
    model.cornerRadius = cornerRadius
  }

  @objc public func attach(to view: UIView) {
    var responder: UIResponder? = view.next
    while let next = responder, !(next is UIViewController) {
      responder = next.next
    }
    guard let parent = responder as? UIViewController, controller.parent !== parent else { return }
    detach()
    parent.addChild(controller)
    controller.didMove(toParent: parent)
  }

  /// Touches synthesized in-process (Detox) get their gesture recognizers by walking the
  /// private `_parentGestureRecognizerContainer` chain, and a SwiftUI host answers nil to
  /// its first query: those touches then reached nothing, not even React Native. Asking
  /// once resolves it. Guarded by `responds(to:)`, so it is a no-op if the selector goes,
  /// and real touches never depend on it. The fix react-native-pager-view ships (#1208).
  @objc public func resolveGestureRecognizerContainers() {
    let selector = NSSelectorFromString("_parentGestureRecognizerContainer")
    var visited = Set<ObjectIdentifier>()
    var current: AnyObject? = container
    while let node = current, node.responds(to: selector), visited.insert(ObjectIdentifier(node)).inserted {
      current = node.perform(selector)?.takeUnretainedValue()
    }
  }

  @objc public func detach() {
    guard controller.parent != nil else { return }
    controller.willMove(toParent: nil)
    controller.removeFromParent()
  }
}

struct ContextMenuContent: View {
  @ObservedObject var model: ContextMenuModel
  let container: UIView

  var body: some View {
    ChildrenView(container: container)
      .frame(maxWidth: .infinity, maxHeight: .infinity)
      .contentShape(
        .contextMenuPreview,
        RoundedRectangle(cornerRadius: model.cornerRadius, style: .circular)
      )
      .contextMenu {
        ForEach(model.items, id: \.id) { item in
          Button(role: item.destructive ? .destructive : nil) {
            model.onSelect?(item.id)
          } label: {
            if item.systemImage.isEmpty {
              Text(item.title)
            } else {
              Label(item.title, systemImage: item.systemImage)
            }
          }
        }
      }
      .ignoresSafeArea()
  }
}

private struct ChildrenView: UIViewRepresentable {
  let container: UIView

  func makeUIView(context: Context) -> UIView { container }

  func updateUIView(_ view: UIView, context: Context) {}
}
