define ([], function () {

    $_DO.update_house_doc_new = function (e) {

        var r = w2ui ['house_doc_new_form'].record;

        if (!r.file_type) {
            $('input[name=file_type]').focus ()
            return alert ('Укажите, пожалуйста, тип документа');
        }

        if (r.file_type.is_not_editable) {
            $('input[name=file_type]').focus ()
            return alert ('Укажите, пожалуйста, тип документа');
        }

        if (!r.dt) {
            $('input[name=dt]').focus ()
            return alert ('Укажите, пожалуйста, дату документа');
        }
        
        if (!/^\d\d\.\d\d\.\d\d\d\d$/.test (r.dt)) {
            $('input[name=dt]').focus ()
            return alert ('Некорректная дата документа');
        }

        if (!r.no) {
            $('input[name=no]').focus ()
            return alert ('Укажите, пожалуйста, номер документа');
        }

        if (!r.files || r.files.length == 0) {
            $('input[name=files]').focus ()
            return alert ('Вы не указали файл');
        };
        
        if (r.note == null) r.note = ''

        var n = r.files.length
        
        var exts   = {pdf:1, doc:1, docx:1, rtf:1, xls:1, xlsx:1, jpg:1, jpeg:1}
        var max_mb = 10
        var sum_size = 0;
        
        for (var i = 0; i < n; i ++) {
        
            var file = r.files [i].file
            
            var fn = file.name

            if (fn.length > 255) return alert ('Некорректное имя файла: ' + fn + '. Согласно требованиям ГИС ЖКХ, его длина не может превышать 255 символов')

            var parts = fn.split ('.')         
            
            if (parts.length < 2) return alert ('Некорректное имя файла: ' + fn + ' (невозможно определить расширение)')

            var ext = parts [parts.length - 1];
            
            if (!exts [ext]) {
            
                var l = []; for (var e in exts) l.push (e)
            
                return alert ('Некорректное имя файла: ' + fn + '.\n\nСогласно требованиям ГИС ЖКХ, разрешены следующие: ' + l.sort ().join (', ') + '.')
            
            }
            
            if (file.size > max_mb * 1024 * 1024) return alert ('Файл ' + fn + ' имеет недопустимо большой объём. Согласно требованиям ГИС ЖКХ, его величина не может превышать ' + max_mb + ' Мб.')
            
            sum_size += file.size

        }
        
        $('#w2ui-popup button').hide ()
        
        var $progress = $('#w2ui-popup progress')
        
        $progress.prop ({max: sum_size, value: 0}).show ()    
        
        var portion = 128 * 1024;
        var sum = 0;

        w2utils.lock ($('#w2ui-popup .w2ui-page'));
        
        var contract = $('body').data ('data')
        
        var data = {
            uuid         : $_REQUEST.id,
            no           : r.no,
            dt           : normalizeValue (r.dt, 'date'),
            note         : r.note,
            id_file_type : r.file_type.id,
        }
        
        var n = r.files.length

        var check = setInterval (function () {
            if (n) return
            clearInterval (check)
            $_DO.apologize = $.noop
            sessionStorage.setItem ('__house_doc_added', 1)
            location.reload ()
        }, 100)

        var opt = {
            data       : data,
            type       : 'house_docs',
            onprogress : function (x, y) {sum += portion; $progress.val (sum)},
            onloadend  : function () {n --}
        }
                   
        $.each (r.files, function () {Base64file.upload (this.file, opt)})
    
    }

    return function (done) {
    
        var house = $('body').data ('data')

        done (house)
        
    }
    
})