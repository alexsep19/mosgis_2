define ([], function () {

    $_DO.update_mgmt_contract_doc_new = function (e) {

        var f = w2ui ['mgmt_contract_doc_new_form']

        var v = f.values ()
        var r = f.record

        if (!v.id_type) die ('id_type', 'Укажите, пожалуйста, тип документа')
        if (v.id_type == 9 && !v.purchasenumber) die ('purchasenumber', 'Укажите, пожалуйста, номер извещения об открытом конкурсе')

        if (!r.files || r.files.length == 0) die ('files', 'Укажите, пожалуйста, файл')                

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
            uuid           : $_REQUEST.id,
            description    : v.description,
            id_type        : v.id_type,
            purchasenumber : v.purchasenumber
        }
        
        var n = r.files.length

        var check = setInterval (function () {
            if (n) return
            clearInterval (check)
            $_DO.apologize = $.noop
            sessionStorage.setItem ('__mgmt_contract_doc_added', 1)
            location.reload ()
        }, 100)

        var opt = {
            data       : data,
            type       : 'contract_docs',
            onprogress : function (x, y) {sum += portion; $progress.val (sum)},
            onloadend  : function () {n --}
        }
                   
        $.each (r.files, function () {Base64file.upload (this.file, opt)})
    
    }

    return function (done) {
    
        done (clone ($('body').data ('data')))
        
    }
    
})