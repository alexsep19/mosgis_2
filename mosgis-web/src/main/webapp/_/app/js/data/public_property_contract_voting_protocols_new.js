define ([], function () {

    $_DO.update_public_property_contract_voting_protocols_new = function (e) {
    
        var g = w2ui ['new_org_works_grid']
        
        var v = {ids: g.getSelection ()}
        
        if (!v.ids.length) die ('foo', 'Вы не выбрали ни одного протокола')
        
        query ({type: 'public_property_contracts', action: 'add_voting_protocols'}, {data: v}, function () {
            w2popup.close ()
            use.block ('public_property_contract_voting_protocols')           
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        var g = w2ui ['public_property_contract_voting_protocols_grid']
        
        var ids = {}; $.each (g.records, function () {ids [this.id] = 1})

        query ({type: 'voting_protocols', id: undefined}, {limit:100000, offset:0, data: {uuid_house: data.item.fiashouseguid}}, function (d) {
        
            data.records = dia2w2uiRecords (d.root.filter (function (r) {return !ids [r.id]}))
            
            data.records.length ? done (data) : alert ('Протоколы всех ОСС по данному адресу уже прикреплены к договору')
            
        })

    }

})