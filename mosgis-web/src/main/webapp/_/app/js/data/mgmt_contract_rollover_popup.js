define ([], function () {

    $_DO.update_mgmt_contract_rollover_popup = function (e) {

        var f = w2ui ['mgmt_contract_rollover_popup_form']

        var v = f.values ()
        
        if (!v.rolltodate) die ('rolltodate', 'Укажите, пожалуйста, дату для продления')

        query ({type: 'mgmt_contracts', action: 'rollover'}, {data: v}, reload_page)
            
    }

    return function (done) {
        
        var data = clone ($('body').data ('data'))        

        var dt_from = new Date (data.item.effectivedate)
        var dt_to   = new Date (data.item.plandatecomptetion)
        
        var d = (dt_to - dt_from) / 1000 / 24 / 60 / 60

        var dt = new Date ()
        dt.setDate (dt.getDate () + d + 1)

        data.record = {rolltodate: dt_dmy (dt.toJSON ())}
    
        done (data)
        
    }
    
})