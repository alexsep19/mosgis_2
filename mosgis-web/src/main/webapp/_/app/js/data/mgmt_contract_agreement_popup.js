define ([], function () {

    $_DO.download_mgmt_contract_agreement_popup = function (e) {    

        var box = $('#w2ui-popup')

        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}

        w2utils.lock (box, label (0, 1))

        download ({

            type:   'contract_docs', 
            id:     w2ui ['mgmt_contract_agreement_popup_form'].record.id,
            action: 'download',

        }, {}, {

            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},

            onload: function () {w2utils.unlock (box)},

        })
    
    }


    $_DO.update_mgmt_contract_agreement_popup = function (e) {
    
        var form = w2ui ['mgmt_contract_agreement_popup_form']
        
        var v = form.values ()
        
        if (!v.agreementdate) die ('agreementdate', 'Укажите, пожалуйста, дату дополнительного соглашения')
        if (!v.agreementnumber) die ('agreementnumber', 'Укажите, пожалуйста, номер дополнительного соглашения')
        
        query ({type: 'contract_docs', action: 'edit', id: form.record.id}, {data: v}, reload_page)

    }

    return function (done) {

        var grid = w2ui ['mgmt_contract_agreements_grid']

        var id = grid.getSelection () [0]

        var data = clone (grid.get (id))
        
        data.agreementdate = dt_dmy (data.agreementdate)

        done (data)

    }

})