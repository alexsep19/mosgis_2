define ([], function () {

    function upload_gis_file ($progress, file, type, data, done) {
    
        $progress.prop ({max: file.size, value: 0}).show ().css ({visibility: 'visible'})
    
        Base64file.upload (file, {
            type: type, 
            data: data, 
            onprogress: function (x, y) {$progress.val (x)},
            onloadend: done
        })
    
    }
    
    $_DO.update_charter_payment_new = function (e) {

        var form = w2ui ['charter_payment_new_form']

        var v = form.values ()
        var r = form.record

        var it = $('body').data ('data').item
        
        if (!v.begindate) die ('begindate', 'Укажите, пожалуйста, дату начала')

        if (!v.enddate) die ('enddate', 'Укажите, пожалуйста, дату окончания')
        if (v.enddate < v.begindate) die ('enddate', 'Дата начала превышает дату окончания управления')
        
        if (v.payment_1) {
            if (!r.file_1) die ('file_1', 'Загрузите, пожалуйста, протокол')
            validate_gis_file ('file_1', r.file_1 [0].file)
        }
        else {
            if (r.file_1) die ('payment_1', 'Вы загрузили протокол, но не указали размер платы')
        }

        if (v.payment_0) {
            if (!r.file_0) die ('file_0', 'Загрузите, пожалуйста, протокол')
            validate_gis_file ('file_0', r.file_0 [0].file)
        }
        else {
            if (r.file_0) die ('payment_0', 'Вы загрузили протокол, но не указали размер платы')
        }

        v.uuid_charter = $_REQUEST.id
        
        form.lock ()

        query ({type: 'charter_payments', action: 'create', id: undefined}, {data: v}, function (data) {
        
            var uuid_charter_payment = data.id
        
            function exit () {
            
                w2popup.close ()

                w2confirm ('Услуга зарегистрирована. Открыть её страницу в новой вкладке?').yes (function () {openTab ('/charter_payment/' + uuid_charter_payment)})

                var grid = w2ui ['charter_payments_grid']

                grid.reload (grid.refresh)
                
            }
            
            function check_file (n, done) {

                var fn = 'file_' + n

                var fl = r [fn]; if (!fl) return done ()

                var file = fl [0].file

                upload_gis_file ($('#progress_' + n), file, 'charter_payment_docs', {uuid_charter_payment: uuid_charter_payment, description: file.name}, function (uuid_file) {

                    var data = {}

                    data ['uuid_file_' + n] = uuid_file

                    query ({type: 'charter_payments', action: 'update', id: uuid_charter_payment}, {data: data}, done)

                })                

            }    
                        
            check_file (1, function () {            
                check_file (0, exit)                
            })
                            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = {
            uuid_charter_object: "",
            begindate: dt_dmy (data.item.date_),
        }
        
        var root = data.charter_objects
                
        var a = []

        if (root.length == 1) {
            data.record.uuid_charter_object = root [0].id
        }
        else {
            a.push ({id: "", text: "Все объекты устава"})
        }
        
        $.each (root, function () {
            a.push (this)
        })
        
        data.objects = a
        
        done (data)

    }

})