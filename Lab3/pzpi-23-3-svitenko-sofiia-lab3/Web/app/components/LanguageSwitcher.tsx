import { useTranslation } from "react-i18next";

export default function LanguageSwitcher() {
  const { i18n } = useTranslation();
  const toggle = () => i18n.changeLanguage(i18n.language === "uk" ? "en" : "uk");
  return (
    <button
      onClick={toggle}
      className="px-3 py-1 text-sm border border-gray-300 rounded-md hover:bg-gray-100 transition-colors font-medium"
    >
      {i18n.language === "uk" ? "EN" : "UA"}
    </button>
  );
}
