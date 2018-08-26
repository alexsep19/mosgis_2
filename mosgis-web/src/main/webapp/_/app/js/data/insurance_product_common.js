define ([], function () {

    var form_name = 'insurance_product_common_form'

    $_DO.open_orgs_insurance_product_common = function (e) {
    
        $('body').data ('voc_organizations_popup.callback', function (r) {
        
            var f = w2ui [form_name]
            
            var d = {id: r.uuid, text: r.label}
                        
            f.record.insuranceorg = d
            
            f.refresh ()

        })

        use.block ('voc_organizations_popup')

    }

    $_DO.download_insurance_product_common = function (e) {
    
        var box = w2ui [form_name].box

        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}

        w2utils.lock (box, label (0, 1))

        download ({

            type:   'insurance_products', 
            id:     $_REQUEST.id,
            action: 'download',

        }, {}, {

            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},

            onload: function () {w2utils.unlock (box)},

        })
    
    }
    
    $_DO.cancel_insurance_product_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'insurance_products'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_insurance_product_common = function (e) {

        var data = {item: w2ui [form_name].record}
        
        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_insurance_product_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
                                
        if (!v.description)             die ('description', 'Опишите, пожалуйста, продукт')
        if (v.description.length > 500) die ('description', 'Максимальная длина описания — 500 символов')

        var r = f.record;

        var refreshAndClose = reload_page

        if (!r.files || r.files.length == 0) {

            if (r.uuid) {            
                return query ({type: 'insurance_products', id: r.uuid, action: 'update'}, {data: v}, refreshAndClose)
            }
            else {
                die ('files', 'Приложите, пожалуйста, файл договора страхования')
            }

        }
        
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
    
    $_DO.delete_insurance_product_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'insurance_products', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.undelete_insurance_product_common = function (e) {   
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return        
        query ({type: 'insurance_products', action: 'undelete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_insurance_product_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('insurance_product_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('insurance_product_common.active_tab') || 'insurance_product_common_log'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']
        
        data.item.status_label = data.vc_async_entity_states [data.item.id_status]
        data.item.insuranceorg_label = data.vc_orgs_ins [data.item.insuranceorg]
        
        data.item.err_text = data.item ['out_soap.err_text']
        
        data.item._can = $_USER.role.admin /*|| data.item.id_status == 10*/ ? {} : {
            edit: 1 - data.item.is_deleted,
            update: 1,
            cancel: 1,
            delete: 1 - data.item.is_deleted,
//            undelete: data.item.is_deleted,
        }

        done (data)
        
    }

})