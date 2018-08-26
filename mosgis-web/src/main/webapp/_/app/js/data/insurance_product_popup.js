define ([], function () {

    var form_name = 'insurance_product_popup_form'

    $_DO.open_orgs_insurance_product_popup = function (e) {
    
        var f = w2ui [form_name]
        
        var saved = {
            data: clone ($('body').data ('data')),
            record: clone (f.record)
        }        
    
        $('body').data ('voc_organizations_popup.callback', function (r) {

            if (r) saved.record.insuranceorg = {id: r.uuid, text: r.label}
            
            $('body').data ('data', saved.data)

            $_SESSION.set ('record', saved.record)

            use.block ('insurance_product_popup')

        })

        use.block ('voc_organizations_popup')

    }

    $_DO.update_insurance_product_popup = function (e) {
    
        var form = w2ui [form_name]

        var v = form.values ()
        
        if (!v.insuranceorg || v.insuranceorg == 'other') die ('insuranceorg', 'Укажите, пожалуйста, стаховую компанию')
        if (!v.description)             die ('description', 'Опишите, пожалуйста, продукт')
        if (v.description.length > 500) die ('description', 'Максимальная длина описания — 500 символов')

        var r = form.record;
        
        function refreshAndClose () {
            w2popup.close ()
            var grid = w2ui ['insurance_products_grid']
            grid.reload (grid.refresh)
        }

        if (!r.files || r.files.length == 0) {

            if (r.uuid) {            
                return query ({type: 'insurance_products', id: r.uuid, action: 'update'}, {data: v}, refreshAndClose)            
            }
            else {
                die ('files', 'Приложите, пожалуйста, файл договора страхования')
            }

        }
        
        var n = r.files.length
        
        var exts   = {pdf:1, doc:1, docx:1, rtf:1, xls:1, xlsx:1, jpg:1, jpeg:1, txt:1}
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
            uuid         : r.uuid,
            insuranceorg : v.insuranceorg,
            description  : v.description,
        }
        
        var n = r.files.length

        var check = setInterval (function () {

            if (n) return

            clearInterval (check)
            refreshAndClose ()
            
        }, 100)

        var opt = {
            data       : data,
            type       : 'insurance_products',
            action     : {update: 'append'},
            onprogress : function (x, y) {sum += portion; $progress.val (sum)},
            onloadend  : function () {n --}
        }
                   
        $.each (r.files, function () {Base64file.upload (this.file, opt)})
    
    }

    return function (done) {
    
        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')

        done (data)
        
    }
    
})