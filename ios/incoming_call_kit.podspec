#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint incoming_call_kit.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'incoming_call_kit'
  s.version          = '0.0.1'
  s.summary          = 'Highly customizable Flutter plugin for incoming call UI with CallKit for iOS.'
  s.description      = <<-DESC
Highly customizable Flutter plugin for incoming call UI.
Custom Android full-screen with gradients and swipe gestures. CallKit for iOS.
Supports incoming, outgoing, missed calls, PushKit/VoIP tokens, and background handling.
                       DESC
  s.homepage         = 'https://github.com/ashiqualii/incoming_call_kit'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Ashique Ali' => 'ashiqualii@gmail.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '13.0'

  s.frameworks = 'CallKit', 'PushKit', 'AVFoundation', 'UserNotifications'

  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'

  s.resource_bundles = {'incoming_call_kit_privacy' => ['Resources/PrivacyInfo.xcprivacy']}
end
