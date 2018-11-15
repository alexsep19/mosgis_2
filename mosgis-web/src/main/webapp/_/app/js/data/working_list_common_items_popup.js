define ([], function () {

    $_DO.update_working_list_common_items_popup = function (e) {
    
        var g = w2ui ['new_org_works_grid']
        
        var v = {ids: g.getSelection ()}
        
        if (!v.ids.length) die ('foo', 'Вы не выбрали ни одной работы')
        
        query ({type: 'working_lists', action: 'add_items'}, {data: v}, function () {
        
            w2popup.close ()

            use.block ('working_list_common_items')
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        var idx = {}; $.each (data.records, function () {idx [this.uuid_org_work] = 1})

        data.record = {voc: dia2w2uiRecords (data.org_works.items.filter (function (i) {
            return !idx [i.id]
        }))}
        
        if (!data.record.voc.length) die ('foo', 'Все работы, имеющиеся в справочнике, в данный перечень уже внесены.')
                
        done (data)

    }

})