define ([], function () {

	$_DO.create_infrastructure_net_piece = function (e) {
        $_SESSION.set ('record', {})
        use.block ('infrastructure_net_pieces_popup')
    }

    $_DO.edit_infrastructure_net_piece = function (e) {
        var g = w2ui ['infrastructure_net_pieces_grid']
        $_SESSION.set ('record', g.get (g.getSelection ()[0]))
        use.block ('infrastructure_net_pieces_popup')
    }

    $_DO.delete_infrastructure_net_piece = function (e) {

        var grid = w2ui ['infrastructure_net_pieces_grid']

        var tia = {type: 'infrastructure_net_pieces', action: 'delete'}
        tia.id = grid.getSelection ()[0]

        query (tia, function (data) {
            grid.reload (grid.refresh)
        })

    }

    return function (done) {        
        
        var layout = w2ui ['topmost_layout']
            
        if (layout) layout.unlock ('main')

        var data = clone ($('body').data ('data'))
        
        done (data)
        
    }
    
})