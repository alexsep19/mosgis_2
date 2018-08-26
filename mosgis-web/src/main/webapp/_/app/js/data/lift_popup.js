define ([], function () {

    $_DO.update_lift_popup = function (e) {

        var form = w2ui ['lift_popup_form']

        var v = form.values ()

        if (!v.uuid_entrance) die ('uuid_entrance', 'Укажите, пожалуйста, номер подъезда')
        if (!v.code_vc_nsi_192) die ('code_vc_nsi_192', 'Укажите, пожалуйста, тип лифта')
        if (!v.factorynum) die ('factorynum', 'Укажите, пожалуйста, заводской номер лифта')
        
        var grid = w2ui ['house_passport_lifts_grid']

        $.each (grid.records, function () {        
            if (this.terminationdate) return;
            if (this.id == form.record.id) return;
            if (this.factorynum != v.factorynum) return;            
            die ('factorynum', 'Лифт с таким заводским номером уже зарегистрирован')
        })        

        query ({type: 'lifts', id: form.record.id, action: 'update'}, {data: v}, reload_page)

    }

    return function (done) {
    
        var data = $('body').data ('data')
        
        var grid = w2ui ['house_passport_lifts_grid']
        
        data.lift = grid.get (grid.getSelection () [0])
        
        if (data.lift.terminationdate) data.lift.terminationdate = dt_dmy (data.lift.terminationdate.substr (0, 10))
        
        done (data)

    }

})