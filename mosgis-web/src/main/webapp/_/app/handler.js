if (window.__LOGOUT__) delete window.__LOGOUT__

requirejs.config ({
    baseUrl: sessionStorage.getItem ('staticRoot') + '/libs',
    paths: {app: '../app/js'}
});

function get_default_url () {

    return '/widgets'

}

function setup_request () {

    if (!$_USER) return

    var parts = location.pathname.split ('/').filter (function (i) {return i})
    
//    if (parts.length < 2 && $_USER && $_USER.role) return redirect (window.name = get_default_url ())

    $_REQUEST = {type: parts [1]}
    
    if (parts [2]) $_REQUEST.id = parts [2]

}

$.fn.w2residebar = function (o) {

    if (w2ui [o.name]) w2ui [o.name].destroy ()

    return this.w2sidebar (o)

}

w2utils.settings = {
    weekStarts       : "M",
    "dataType"       : "JSON",
    "locale"         : "ru-RU",
    "dateFormat"     : "dd.mm.yyyy",
    "timeFormat"     : "h24",
    "currency"       : "^[-+]?[0-9]*[\\,]?[0-9]+$",
    "currencyPrefix" : "",
    "currencySuffix" : " р.",
    "currencyPrecision": 2,
    "decimalSymbol"  : ",",
    "groupSymbol"    : " ",
    "float"          : "^[-]?[0-9]*[\\.]?[0-9]+$",
    "shortmonths"    : ["Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"],
    "fullmonths"     : ["Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"],
    "shortdays"      : ["П", "В", "С", "Ч", "П", "С","В"],
    "fulldays"       : ["Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"],
    "phrases" : {
        "Add new record": "Добавить новую запись",
        "Add New": "Добавить",
        "All Fields": "Все поля",
        "Are you sure you want to delete selected records?": "Вы действительно хотите удалить выделенные записи?",
        "Attach files by dragging and dropping or Click to Select": "Перетащите файлы сюда или нажмите чтобы выбрать",
        "begins with": "начинается с",
        "begins": "начинается",
        "before": "до (включительно)",
        "after": "начиная с",
        "between": "между",
        "buffered": "буфер",
        "Clear Search": "Очистить поиск",
        "Column": "Колонка",
        "Confirmation": "Подтверждение",
        "contains": "содержит",
        "Current Date & Time": "Текущие дата и время",
        "Delete Confirmation": "Подтверждение удаления",
        "Delete selected records": "Удалить выбранные записи",
        "Delete": "Удалить",
        "Edit selected record": "Изменить выделенную запись",
        "Edit": "Изменить",
        "Empty list": "Пустой список",
        "ends with": "заканчивается на",
        "ends": "заканчивается",
        "Hide": "Скрыть",
        "in": "в списке",
        "is": "равняется",
        "Loading...": "Загрузка...",
        "Multi Fields": "Несколько полей",
        "Multiple Fields": "Несколько полей",
        "more than": "более или ровно",
        "less than": "менее или ровно",
        "Name": "Имя",
        "Size": "Размер",
        "Type": "Тип",
        "Modified": "Дата изменения",
        "No items found": "Ничего не найдено",
        "No matches": "Совпадений не найдено",
        "No": "Нет",
        "none": "пусто",
        "not null": "не пусто",
        "null": "пусто",
        "Not a float": "Не натуральное число",
        "Not a hex number": "Не шестнадцатеричное число",
        "Not a valid date": "Неверный формат",
        "Not a valid email": "Неверный e-mail",
        "Not alpha-numeric": "Не буквенно-цифровой текст",
        "Not an integer": "Не целое число",
        "Not in money format": "Не денежный формат",
        "not in": "не в списке",
        "Notification": "Уведомление",
        "of": "из",
        "Ok": "OK",
        "Open Search Fields": "Открыть поля поиска",
        "Record ID": "Запись",
        "Records": "Записей",
        "Refreshing...": "Обновление...",
        "Reload data in the list": "Обновить список",
        "Remove": "Удалить",
        "Required field": "Обязательное поле",
        "Reset Column Size": "Восстановить размер колонок",
        "Reset": "Очистить",
        "Return data is not in JSON format. See console for more information.": "Возвращенные данные не в формате JSON. Смотрите в консоли ошибки.",
        "Save changed records": "Сохранить измененные записи",
        "Save": "Сохранить",
        "Saving...": "Сохранение",
        "Search took": "Поиск занял",
        "Search": "Поиск",
        "Search...": "Поиск...",
        "sec": "сек",
        "Select Search Field": "Выбрать поля поиска",
        "selected": "выделено",
        "Select Hour": "Выберите час",
        "Select Minute": "Выберите минуту",
        "Server Response": "Ответ сервера",
        "Show": "Показать",
        "Show/hide columns": "Показать/скрыть колонки",
        "Skip": "Пропустить",
        "Sorting took": "Сортировка заняла",
        "Toggle Line Numbers": "Вкл/Выкл. номера строк",
        "Yes": "Да",
        "Yesterday": "Вчера",
        "Line #": "Номер строки #",
        "Save Grid State": "Сохранить состояние таблицы",
        "Restore Default State": "Восстановить состояние таблицы",
        "Type to search...": "Введите строку поиска...",
        "Your remote data source record count has changed, reloading from the first record.": "Данные изменены на сервере. Пожалуйста, перезагрузите страницу"
    }
}

function dt_dmy    (v) { return !v ? '' : v.substr (0, 10).split ('-').reverse (). join ('.') }

function dt_dmy2   (v) { 
    if (!v) return ''
    var dmy = v.split ('-').reverse ()
    dmy [2] %= 100
    return dmy.join ('.')
}

function _dt (record, ind, col_ind, data) {
    return dt_dmy (data)
}

function _ts (record, ind, col_ind, data) {
    return dt_dmy (data) + data.substr (10)
}

function dt_dmyhm (v) { return !v ? '' : dt_dmy (v.substr (0, 10)) + v.substr (10,6)}

function dt_dmyhms (v) { return !v ? '' : dt_dmy (v.substr (0, 10)) + v.substr (10,9)}

function __d (data) {

    for (i in data) {
    
        if (i.match (/^dt/)) {

            var v = data [i]

            if (!v || v.length != 10 || !v.match (/^\d\d\d\d-\d\d-\d\d$/)) continue

            data [i] = dt_dmy (v)

        }        
    
    }
    
    return data

}

function refreshButtons (e) {e.done (
                
    function () {

        $('button', $(this.box)).each (function () {

            var $this = $(this)
        
            clickOn ($this, $_DO [this.name + '_' + $this.attr ('data-block-name')])
            
        })

        clickOn ($('span.anchor'), onDataUriDblClick)

    }
                
)}

function die (name, text) {
    alert (text)
    $('[name=' + name + ']').focus ()
    throw 'core.ok.validation'
}

function not_off (i) {return !i.off}

function reload_page () { location.reload () }

var base_href = '/mosgis';

function get_nsi (ids, done) {

    var data = $('body').data ('data')

    query ({type: 'voc_nsi_list', part: 'vocs', id: undefined}, {data: {ids: ids}}, function (d) {

        d.bool = [{id: "0", label: "Нет"},{id: "1", label: "Да"}]

        add_vocabularies (d, d)

        for (k in d) data [k] = d [k]

        done (data)

    })

}

function edit_failed (grid, e) {

    return function (o) {
    
        if (o.data && o.data.field) {
            grid.unlock ()
            alert (o.data.message)
            e.preventDefault ()
            grid.editField (e.recid, e.column, e.value_original)
        }
        else {
            $_DO.apologize (o)
        }
        
    }
    
}

function color_data_mandatory (e) {

    var grid = w2ui [e.target]

    $('tr[recid] td[data-mandatory]').each (function () {
        var $this = $(this)
        if ($this.text ()) return
        var p = {title: 'Обязательное поле'}
        $this.css ({background: '#ffcccc'}).prop (p)
        $('*', $this).prop (p)
    })

    $('tr[recid]').each (function () {
        var $this = $(this)
        var r = grid.get ($this.attr ('recid'))
        if (!r) return
        if (r.is_annuled) $('td', $this).css ({background: '#ccc'})
        if (r.id_status == 30) $('td', $this).css ({background: '#fdd'})
        if (r.is_deleted) $('td div', $this).css ({'text-decoration': 'line-through'})
    })

}

function getPluralType () {
    var parts = $_REQUEST.type.split ('_')
    if (parts.length == 2 && parts [0] == 'premise') return parts.join ('s_')
    return $_REQUEST.type + 's'
}

function clickActiveTab (tabs, key) {

    function getActiveId (tabs, key) {

        if (!tabs || !tabs.length) return null

        var id = localStorage.getItem (key)

        for (var i = 1; i < tabs.length; i ++) if (id == tabs [i].id && !tabs [i].hidden) return id

        return tabs [0].id

    }
    
    var id = getActiveId (tabs.tabs, key)
    
    if (id) tabs.click (id)

}

function color_passport_grid (e) {

    e.done (function () {
        
        var last = null
    
        $.each ($('body').data ('data').vc_pass_fields, function () {

            if (!('id_type' in this)) {
            
                var sel = 'tr[recid=' + this.id + ']'

                $(sel + ' td.w2ui-grid-data').css ({
                    'font-weight': 'bold',
                    'border-bottom-width': '1px',
                }).eq (0).css ({'border-right-width': 0})
                
                if (last) {
                
                    sel = 'tr[recid=' + last + ']'

                    $(sel + ' td.w2ui-grid-data').css ({
                        'border-bottom-width': '1px',
                    })
                
                }
            
            }
            
            last = this.id                        

        })
        
        $('td[data-status]').each (function () {
            var $this = $(this)
            var txt = $this.text ()
            if (txt) return $this.find ('*').addBack ().attr ('title', txt)
        })
    
    }) 

}

function prettifyXml (sourceXml) {
    var xmlDoc = new DOMParser().parseFromString(sourceXml, 'application/xml');
    var xsltDoc = new DOMParser().parseFromString([
        // describes how we want to modify the XML - indent everything
        '<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">',
        '  <xsl:output omit-xml-declaration="yes" indent="yes"/>',
        '    <xsl:template match="node()|@*">',
        '      <xsl:copy><xsl:apply-templates select="node()|@*"/></xsl:copy>',
        '    </xsl:template>',
        '</xsl:stylesheet>',
    ].join('\n'), 'application/xml');

    var xsltProcessor = new XSLTProcessor();    
    xsltProcessor.importStylesheet(xsltDoc);
    var resultDoc = xsltProcessor.transformToDocument(xmlDoc);
    var resultXml = new XMLSerializer().serializeToString(resultDoc);
    return resultXml;
};    

function get_valid_gis_file (v, name) {

    var fl = v [name]
    
    if (!fl) die (name, 'Укажите, пожалуйста, файл')
    
    var file = fl [0].file

    if (file.size == 0) die (name, 'Пожалуйста, укажите не пустой файл')

    var magic_number = fl [0].content.slice (0, 6)
    
    validate_gis_file (name, file, magic_number)
    
    return file
    
}

function validate_gis_file (name, file, magic) {

    function getExtFromSignature (signature) {
        switch (signature) {
            case '25504446':
                return 'pdf'
            case 'D0CF11E0':
                return 'doc/xls'
            case '504B0304':
                return 'docx/xlsx'
            case '7B5C7274':
                return 'rtf'
            case 'FFD8FFDB':
            case 'FFD8FFE0':
            case 'FFD8FFE1':
            case 'FFD8FFEE':
                return 'jpg/jpeg'
            default:
                return undefined
        }
    }

    function getExt (str) {
        const b64 = atob (str)
        let bytes = []
        for (var i=0, strLen=b64.length; i < strLen; i++) {
            bytes.push (('0' + b64.charCodeAt(i).toString (16)).substr(-2))
        }
        const hex = bytes.join('').toUpperCase ()
        return getExtFromSignature (hex)
    }

    var filename = file.name
    var filetype = getExt (magic)
    var max_mb = 10
    var exts   = {pdf:1, doc:1, docx:1, rtf:1, xls:1, xlsx:1, jpg:1, jpeg:1}
    var l = []; for (var e in exts) l.push (e)

    if (filetype == undefined) die (name, 'Некорректный тип файла: ' + filename + '. Согласно требованиям ГИС ЖКХ, разрешены следующие: ' + l.sort ().join (', ') + '.')
    if (filename.length > 255) die (name, 'Некорректное имя файла: ' + filename + '. Согласно требованиям ГИС ЖКХ, его длина не может превышать 255 символов')

    var parts = filename.split ('.')
    if (parts.length < 2) die (name, 'Некорректное имя файла: ' + filename + ' (невозможно определить расширение)')

    var ext = parts [parts.length - 1];
    if (!exts [ext]) die (name, 'Некорректное имя файла: ' + filename + '.\n\nСогласно требованиям ГИС ЖКХ, разрешены следующие: ' + l.sort ().join (', ') + '.')

    var possible_exts = filetype.split ('/')
    if (possible_exts.indexOf (ext) < 0) die (name, 'Некорректное имя файла: ' + filename + ' (расширение не соответствует типу)')

    if (file.size > max_mb * 1024 * 1024) die (name, 'Файл ' + filename + ' имеет недопустимо большой объём. Согласно требованиям ГИС ЖКХ, его величина не может превышать ' + max_mb + ' Мб.')

}

function valid_inn(inn) {

    return /^(\d{10}|\d{12})$/.test(inn)

}

function valid_kpp(kpp) {

    return /^\d{9}$/.test(kpp)

}

function show_popup_progress (file_size) {

    w2utils.lock ($('#w2ui-popup .w2ui-page'));

    $('#w2ui-popup button').hide ()        

    var $progress = $('#w2ui-popup progress')        

    $progress.prop ({max: file_size, value: 0}).show ()
    
    return function (x) {$progress.val (x)}

}

requirejs (['elu/elu', 'elu_w2ui/elu_w2ui'], function (jq, elu, elu_w2ui) {
    
    var _redirect = redirect;
    redirect = function (url) {
        _redirect (base_href + url)
    }

    clearTimeout (window.alarm)

    $_SESSION.beforeExpiry ($_SESSION.keepAlive)
    
    window.addEventListener ('storage', $_SESSION.closeAllOnLogout)

    if ($_USER) {

        $_USER.has_nsi_20 = function () {
            for (var i = 0; i < arguments.length; i ++) if ($_USER.role ['nsi_20_' + arguments [i]]) return true
            return false
        }

        $_USER.is_building_society = function () {
            return $_USER.has_nsi_20 (19, 20, 21, 22)
        }

    }

    if ($_USER && $_USER.opt && $_USER.opt.no_tabs) {
        
        openTab = function (url, name) {
            url = base_href + url
            window.name = name || url
            location = url
        }
        
    }
    else {
        
        var _openTab = openTab
        
        openTab = function (url, name) {
            _openTab (base_href + url, name)
        }
        
    }

    setup_request ()

//    use.block ($_REQUEST.type || 'main')

    if (!$_USER) return use.block ('login') 

    use.block ('page')

});