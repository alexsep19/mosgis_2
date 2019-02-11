define ([], function () {

    $_DO.update_account_individual_service_popup = function (e) {
/*
        var form = w2ui ['voc_user_form']

        var v = form.values ()
        var r = form.record
        
        if (!v.fiashouseguid) die ('fiashouseguid', 'Укажите, пожалуйста, адрес обслуживаемого дома')

        if (v.enddate) {
            if (v.enddate < v.startdate) die ('enddate', ' Дата начала управления превышает дату окончания управления')
        }
        
        v.uuid_charter = $_REQUEST.id

        if (v.id_reason == 1) {

            var grid = w2ui ['charter_objects_grid']        

            query ({type: 'charter_objects', action: 'create', id: undefined}, {data: v}, function () {
                w2popup.close ()
                grid.reload (grid.refresh)           
                return
            })

        }
        else {

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
                        
            query ({type: 'charter_objects', action: 'create', id: undefined}, {data: v}, function (d) {

                var data = {
                    uuid                : $_REQUEST.id,
                    uuid_charter_object : d.id,
                    description         : v.description,
                    id_type             : 8,
                }
                
                var n = r.files.length

                var check = setInterval (function () {
                    if (n) return
                    clearInterval (check)
                    $_DO.apologize = $.noop
                    reload_page ()
                }, 100)

                var opt = {
                    data       : data,
                    type       : 'charter_docs',
                    onprogress : function (x, y) {sum += portion; $progress.val (sum)},
                    onloadend  : function () {n --}
                }

                $.each (r.files, function () {Base64file.upload (this.file, opt)})

            })            

        }
*/
    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = {
/*        
            id_reason: 1,
            ismanagedbycontract: 0,
            startdate: dt_dmy (data.item.date_),
*/            
        }
        
        done (data)

    }

})