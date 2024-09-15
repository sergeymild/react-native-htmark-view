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
  `The package 'react-native-htmark-view' doesn't seem to be linked. Make sure: \n\n` +
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

type HtMarkViewProps = {
  params: HtmlParams | MarkdownParams;
  onLinkPress?: (link: string) => void;
  onPress?: () => void;
  style?: ViewStyle;
};

const ComponentName = 'HtMarkView';

const _HtMarkView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<HtMarkViewProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

export const HtMarkView: React.FC<HtMarkViewProps> = memo((props) => {
  const htmlRef = useRef<any>(null);
  if (!props.onLinkPress && !props.onPress) {
    return <_HtMarkView {...props} />;
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
      <_HtMarkView {...props} ref={htmlRef} />
    </TouchableOpacity>
  );
});
