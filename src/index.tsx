import {
  requireNativeComponent,
  UIManager,
  Platform,
  type ViewStyle,
  type TextProps,
  TouchableOpacity,
  findNodeHandle,
  NativeModules,
} from 'react-native';
import React, { memo, useRef } from 'react';

const LINKING_ERROR =
  `The package 'react-native-html-view' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

type CommonParams = {
  maxLines?: number;
  ellipsize?: TextProps['ellipsizeMode'];
};

type HtmlParams = {
  html: string;
} & CommonParams;

type MarkdownParams = {
  markdown: string;
} & CommonParams;

type HtmlViewProps = {
  params: HtmlParams | MarkdownParams;
  onLinkPress?: (link: string) => void;
  onPress?: () => void;
  style?: ViewStyle;
};

const ComponentName = 'HtmlView';

const _HtmlView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<HtmlViewProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

const _MarkdownView =
  UIManager.getViewManagerConfig('MarkdownView') != null
    ? requireNativeComponent<HtmlViewProps>('MarkdownView')
    : () => {
        throw new Error(LINKING_ERROR);
      };

export const HtmlView: React.FC<HtmlViewProps> = memo((props) => {
  const htmlRef = useRef<any>(null);
  if (!props.onLinkPress && !props.onPress) {
    return <_HtmlView {...props} />;
  }
  return (
    <TouchableOpacity
      onPress={async (e) => {
        const tag = findNodeHandle(htmlRef.current);
        if (!tag) return;
        const link = await NativeModules.HtmlView.findLink(
          tag,
          e.nativeEvent.locationX,
          e.nativeEvent.locationY
        );
        if (link) props.onLinkPress?.(link);
        else props.onPress?.();
      }}
    >
      {/*@ts-ignore*/}
      <_HtmlView {...props} ref={htmlRef} />
    </TouchableOpacity>
  );
});

export const MarkdownView: React.FC<HtmlViewProps> = memo((props) => {
  const htmlRef = useRef<any>(null);
  if (!props.onLinkPress && !props.onPress) {
    return <_MarkdownView {...props} />;
  }
  return (
    <TouchableOpacity
      onPress={async (e) => {
        const tag = findNodeHandle(htmlRef.current);
        if (!tag) return;
        const link = await NativeModules.MarkdownView.findLink(
          tag,
          e.nativeEvent.locationX,
          e.nativeEvent.locationY
        );
        if (link) props.onLinkPress?.(link);
        else props.onPress?.();
      }}
    >
      {/*@ts-ignore*/}
      <_MarkdownView {...props} ref={htmlRef} />
    </TouchableOpacity>
  );
});
