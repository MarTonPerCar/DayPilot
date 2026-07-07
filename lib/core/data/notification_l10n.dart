import '../../l10n/app_localizations.dart';
import '../../l10n/locale_notifier.dart';

/// Repositories have no BuildContext, so this looks up the locale directly.
AppLocalizations currentL10n() => lookupAppLocalizations(dayPilotLocaleNotifier.value);
