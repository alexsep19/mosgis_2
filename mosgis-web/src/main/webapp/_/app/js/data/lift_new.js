define ([], function () {

    $_DO.update_lift_new = function (e) {

        var form = w2ui ['lift_new_form']

        var v = form.values ()

        if (!v.uuid_entrance) die ('uuid_entrance', 'Укажите, пожалуйста, номер подъезда')
        if (!v.code_vc_nsi_192) die ('code_vc_nsi_192', 'Укажите, пожалуйста, тип лифта')
        if (!v.factorynum) die ('factorynum', 'Укажите, пожалуйста, заводской номер лифта')

        var grid = w2ui ['house_passport_lifts_grid']

        $.each (grid.records, function () {        
            if (this.terminationdate) return;
            if (this.factorynum != v.factorynum) return;            
            die ('factorynum', 'Лифт с таким заводским номером уже зарегистрирован')
        })

        query ({type: 'lifts', id: undefined, action: 'create'}, {data: v}, reload_page)

    }

    return function (done) {

        var data = $('body').data ('data')

        done (data)

    }

})