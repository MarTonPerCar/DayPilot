import '../../l10n/app_localizations.dart';
import '../../l10n/locale_notifier.dart';

/// Repositories have no BuildContext, so notification title/body text (which
/// gets baked into the DB as plain strings, same as the Android app) is
/// localized against the app's current locale directly via the generated
/// lookup, rather than AppLocalizations.of(context).
AppLocalizations currentL10n() => lookupAppLocalizations(dayPilotLocaleNotifier.value);
