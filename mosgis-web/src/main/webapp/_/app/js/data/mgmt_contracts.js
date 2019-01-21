define ([], function () {

    function setDeleted (action) {

        var grid = w2ui ['mgmt_contracts_grid']
        
        var id = grid.getSelection () [0]

        var tia = {
            type:   'mgmt_contracts', 
            id:     id,
            action: action
        }

        query (tia, {}, function () {
            grid.reload (function () {
                grid.refresh ()            
                grid.selectNone ()
                grid.select (id)
            })
        })

    }
    
    $_DO.import_objects_mgmt_contracts = function (e) {
        use.block ('mgmt_contract_objects_import_popup')
    }

    $_DO.create_mgmt_contracts = function (e) {
        $_SESSION.set ('record', {})
        use.block ('mgmt_contract_popup')
    }

    $_DO.edit_mgmt_contracts = function (e) {       

        var grid = w2ui [e.target]

        var r = grid.get (e.recid)
        
        if (r.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        $_SESSION.set ('record', r)

        use.block ('mgmt_contract_popup')

    }

    return function (done) {
    
        var layout = w2ui ['rosters_layout']

        if (layout) layout.unlock ('main')

        query ({type: 'mgmt_contracts', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            $('body').data ('data', data)

            done (data)

        }) 

    }

})