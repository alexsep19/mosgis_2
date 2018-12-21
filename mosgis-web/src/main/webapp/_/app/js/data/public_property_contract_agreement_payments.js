define ([], function () {

    $_DO.create_public_property_contract_agreement_payments = function (e) {
    
        var it = $('body').data ('data').item
    
        datefrom = new Date ()
        datefrom.setDate (1)
        datefrom = datefrom.toISOString ()
        if (datefrom < it.startdate.substr (0, 10)) datefrom = it.startdate
        
        dateto = new Date ()
        dateto.setMonth (1 + dateto.getMonth ())
        dateto.setDate (0)
        dateto = dateto.toISOString ()
        if (dateto > it.enddate.substr (0, 10)) dateto = it.enddate

        $_SESSION.set ('record', {
            datefrom: datefrom,
            dateto: dateto,
            bill: 0,
            debt: 0,
            paid: 0,
        })

        use.block ('agreement_payment_popup')
        
    }
    
    $_DO.approve_public_property_contract_agreement_payments = function (e) {
        
        var grid = w2ui ['public_property_contract_agreement_payments_grid']
        var id = grid.getSelection () [0]
        var r = grid.get (id)
        
        if (!confirm ('Отправить в ГИС информацию об оплате с ' + dt_dmy (r.datefrom) + ' по ' + dt_dmy (r.dateto) + '?')) return
        
        grid.lock ()
        
        query ({type: 'agreement_payments', id: id, action: 'approve'}, {}, function () {
            grid.reload (grid.refresh)
        })
        
    }
    
    $_DO.edit_public_property_contract_agreement_payments = function (e) {    
    
        var grid = w2ui ['public_property_contract_agreement_payments_grid']
        var id = grid.getSelection () [0]
        var r = grid.get (id)
       
        switch (r.id_ap_status) {        
        
            case 10:
                $_SESSION.set ('record', grid.get (id))        
                return use.block ('agreement_payment_popup')
            case 40:
                if (!confirm ('Вернуть на редактирование информацию об оплате с ' + dt_dmy (r.datefrom) + ' по ' + dt_dmy (r.dateto) + '?')) return
            case 14:
                grid.lock ()
                query ({type: 'agreement_payments', id: id, action: 'alter'}, {}, function () {
                    grid.unlock ()
                    grid.reload (grid.refresh)
                })

        }
        
    }
    
    $_DO.delete_public_property_contract_agreement_payments = function (e) {
    
        var grid = w2ui ['public_property_contract_agreement_payments_grid']
        var id = grid.getSelection () [0]
        var r = grid.get (id)
        
        if (!confirm ('Удалить информацию об оплате с ' + dt_dmy (r.datefrom) + ' по ' + dt_dmy (r.dateto) + '?')) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()

        query ({type: 'agreement_payments', id: id, action: 'delete'}, {}, reload_page)
        
    }

    return function (done) {
        w2ui ['topmost_layout'].unlock ('main')               
        var data = clone ($('body').data ('data'))
        done (data)
    }

})