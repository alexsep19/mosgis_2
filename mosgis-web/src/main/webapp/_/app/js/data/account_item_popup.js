define ([], function () {

    $_DO.update_account_item_popup = function (e) {

        var form = w2ui ['account_item_popup_form']

        var v = form.values ()
        
        if (!v.fiashouseguid) die ('fiashouseguid', 'Укажите, пожалуйста, адрес')
        if (!(v.sharepercent >= 0.01)) die ('sharepercent', 'Укажите, пожалуйста, корректное значение доли в процентах')
        
        v.uuid_account = $_REQUEST.id

        query ({type: 'account_items', id: undefined, action: 'create'}, {data: v}, function (data) {        
            w2popup.close ()                        
            var grid = w2ui ['account_common_items_grid']
            grid.reload (grid.refresh)
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record') || {
            sharepercent: 100,
        }
                
        var tia = {id: undefined}
        var p   = {limit: 100000, offset:0}
        
        var it = data.item

        if (it.uuid_contract) {
        
            tia.type = 'contract_objects'
            
            p.search = [{
                field:    "uuid_contract",
                operator: "is",
                value:    it.uuid_contract
            }]
            
        }
        
        query (tia, p, function (d) {

            data.fias = d.root.map (function (i) {return {
                id: i.fiashouseguid,
                text: i ['fias.label'],
            }})

            if (!data.record.fiashouseguid && data.fias.length == 1) data.record.fiashouseguid = data.fias [0].id

            done (data)

        })

    }

})