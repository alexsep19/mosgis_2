define ([], function () {

    $_DO.create_mgmt_contract_object_house_passport = function (e) {

        var form = w2ui ['mgmt_contract_object_new_house_passport_form']

        var v = form.values ()
        
        console.log (v)
        
        var tia = {type: 'mgmt_contract_object'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу паспорта дома?').yes (function () {openTab ('/house_passport/' + data.id)})
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})